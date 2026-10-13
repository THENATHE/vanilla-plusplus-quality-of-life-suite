"""Actual Sorted controls and cursor clicks against authoritative disposable inventories."""
import json
from collections import Counter


def run_layout_checks(name, process, read, wait, control):
    checks, scenarios = [], []
    action_id = request_id = 0

    def check(condition, message, details=None):
        if not condition:
            raise AssertionError(f'{message}: {details}')
        checks.append(message)

    def act(kind, **values):
        nonlocal action_id
        action_id += 1
        (control / f'{name}-action.json').write_text(json.dumps(dict(type=kind, id=action_id, **values)))
        wait(lambda: any(row.get('id') == action_id for row in read(name, 'client').get('actions', [])), process, kind)
        return next(row for row in read(name, 'client')['actions'] if row['id'] == action_id)

    def settle(ticks=15):
        current = read(name, 'client')['ticks']
        wait(lambda: read(name, 'client').get('ticks', 0) >= current + ticks, process, 'layout settles')

    def prepare(kind):
        nonlocal request_id
        request_id += 1
        (control / f'{name}-layout-server-request.json').write_text(json.dumps(dict(type=kind, id=request_id)))
        cursor = 4 if kind == 'cursor' else 0
        wait(lambda: read(name, 'server').get('prepared_id') == request_id
             and read(name, 'client').get('cursor') == cursor, process, f'authoritative {kind} fixture')

    def open_menu(tier, size):
        previous = read(name, 'client').get('menu_id')
        capacity = 27 if tier == 'leather' else 78
        (control / f'{name}-open.json').write_text(json.dumps(dict(size=size, tier=tier)))
        wait(lambda: read(name, 'client').get('menu_id') != previous
             and read(name, 'client').get('size') == size
             and read(name, 'client').get('capacity') == capacity
             and read(name, 'server').get('menu_id') == read(name, 'client').get('menu_id'), process, 'actual backpack opens')
        settle()

    def totals(row):
        return Counter(row['storage']) + Counter(row['inventory'])

    for tier in ('leather', 'netherite'):
        for size in (0, 2, 3):
            label = f'{tier} {size or "plain"}'
            open_menu(tier, size)
            registered = act('register')
            expected_id = 'tiered_backpacks:backpack_menu' if size == 0 else 'vpp_backpack_crafting:crafting'
            check(registered['registered_menu_id'] == expected_id, label + ': actual /sorted storage and /sorted storage add use the real menu ID', registered)
            wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'registered menu closes')
            open_menu(tier, size)
            act('resize', width=640, height=400)
            if size:
                act('book-state', open=False)
            settle()
            validation = act('validate')
            check(validation['filtered_grid_and_result_excluded'], label + ': Sorted storage classifier and filtering view exclude grid and result')
            before = read(name, 'server')
            act('sort')
            wait(lambda: not read(name, 'client').get('sort_running', True)
                 and read(name, 'server').get('diamond_stacks') == 1, process, 'actual Sorted container sort merges storage diamonds')
            sorted_row = read(name, 'server')
            check(totals(sorted_row) == totals(before), label + ': container sort conserves authoritative inventory and storage', sorted_row)
            check(sorted_row['inventory'] == before['inventory'], label + ': container sort leaves player inventory unchanged')
            if size:
                check((sorted_row['grid'], sorted_row['result']) == (before['grid'], before['result']), label + ': sort leaves named grid sentinel and craftable output unchanged')
                check(sorted_row['holder'] == sorted_row['storage'], label + ': sort synchronizes the exact backpack holder')
            act('stack')
            expected_storage = {'minecraft:diamond': 15, 'minecraft:cobblestone': 20, 'minecraft:emerald': 2,
                                'minecraft:oak_log': 8, 'minecraft:oak_planks': 16}
            wait(lambda: not read(name, 'client').get('sort_running', True)
                 and read(name, 'server').get('storage') == expected_storage, process, 'actual Sorted quick stack moves only matching items into storage')
            stacked = read(name, 'server')
            check(totals(stacked) == totals(before), label + ': quick stack conserves authoritative inventory and storage', stacked)
            check(stacked['inventory'].get('minecraft:feather') == 4, label + ': quick stack preserves unrelated player items')
            if size:
                check((stacked['grid'], stacked['result']) == (before['grid'], before['result']), label + ': quick stack excludes named grid and output even when storage contains matching wood')
                check(stacked['holder'] == stacked['storage'], label + ': quick stack synchronizes holder components')
            act('close')
            wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'close persists backpack')
            check(read(name, 'server')['held_bag'] == expected_storage, label + ': menu close preserves exact stored contents')
            open_menu(tier, size)
            states = [(width, opened) for width in (640, 427) for opened in ((False, True) if size else (False,))]
            for width, opened in states:
                state = f'{label} width={width} book={opened}'
                act('resize', width=width, height=240 if tier == 'netherite' and size else 300)
                if size:
                    act('book-state', open=opened)
                settle()
                validation = act('validate')
                check(validation['sorted_buttons_verified'] == 7, state + ': all Sorted controls follow actual storage/player anchors and fit the viewport')
                overlay = validation['narrow'] and opened
                points = ['storage', 'outside'] + (['crafting', 'gap', 'above', 'below'] if size else [])
                if opened:
                    points += ['book', 'book-tab']
                    if validation['pinned_tab_visible']:
                        points.append('pins')
                observations = []
                for point in points:
                    prepare('cursor')
                    before_click = read(name, 'server')
                    click = act('point', point=point)
                    if point == 'pins':
                        check(click['pinned_tab_selected'], state + ': actual BRBR pinned-tab callback selects the pinned recipe view')
                    drops = point in ('outside', 'gap', 'above', 'below') and not click['book_intersects_point']
                    wait(lambda: read(name, 'server').get('cursor') == (0 if drops else 4)
                         and read(name, 'server').get('dropped_emeralds') == (4 if drops else 0)
                         and read(name, 'client').get('cursor') == (0 if drops else 4), process, state + ' cursor ' + point)
                    row = read(name, 'server')
                    check(row['storage'] == before_click['storage'] and row['inventory'] == before_click['inventory'], state + ': ' + point + ' preserves all stored and player items')
                    if size:
                        check((row['grid'], row['result']) == (before_click['grid'], before_click['result']), state + ': ' + point + ' leaves crafting inputs/result untouched')
                    check(row['cursor'] + row['dropped_emeralds'] == 4, state + ': ' + point + ' preserves the exact cursor count')
                    check(row['dropped_emeralds'] == (4 if drops else 0), state + ': ' + point + (' drops outside both panels' if drops else ' protects a painted panel or visible book region'))
                    observations.append(dict(point=point, dropped=drops, x=click['x'], y=click['y'],
                                             book_intersects_point=click['book_intersects_point'], book_bounds=click.get('book_bounds')))
                prepare('reset')
                scenarios.append(dict(tier=tier, grid=size, width=width, book_open=opened,
                                      native_narrow_overlay=overlay, validation=validation, cursor_clicks=observations))
                if size == 3 and (width == 640 or tier == 'netherite' and opened):
                    act('screenshot', name=f'layout-{tier}-{size}-{width}-book-{str(opened).lower()}')
            act('close')
            wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'layout fixture closes')
    return dict(profile='suite', assertion_count=len(checks), checks=checks, scenarios=scenarios)
