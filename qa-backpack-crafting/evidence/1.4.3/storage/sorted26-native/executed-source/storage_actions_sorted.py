"""Real Sorted buttons and recovery selection, with authoritative crafting-slot checks."""
import json
from collections import Counter


def run_storage_checks(name, process, read, wait, control, baseline=False, placement_only=False):
    checks, operations, recovery, button_offsets = [], [], [], []
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

    def settle():
        tick = read(name, 'client')['ticks']
        wait(lambda: read(name, 'client').get('ticks', 0) >= tick + 15
             and not read(name, 'client').get('sort_running', True), process, 'storage operation settles')

    def prepare(kind, **values):
        nonlocal request_id
        request_id += 1
        (control / f'{name}-layout-server-request.json').write_text(json.dumps(dict(type=kind, id=request_id, **values)))
        cursor = 4 if kind == 'recovery-reset' else 0
        wait(lambda: read(name, 'server').get('prepared_id') == request_id
             and read(name, 'client').get('cursor') == cursor, process, 'authoritative storage fixture')
        settle()

    def totals(row):
        return Counter(row['storage']) + Counter(row['inventory'])

    icons = ['sort_container', 'sort_inventory', 'quick_stack', 'quick_fill', 'quick_empty', 'quick_dump']
    cases = [(icon, False) for icon in icons] + [('quick_empty', True), ('quick_dump', True)]
    blocked = ['origin-grid', 'matching-grid', 'empty-grid']
    scenarios = blocked + ['storage-partial', 'player-partial', 'storage-empty', 'player-empty']

    for tier in (('leather',) if baseline else ('leather', 'netherite', 'large')):
        if not baseline:
            act('backpack-size', rows=9 if tier == 'large' else 6, columns=13)
        for size in (2, 3):
            label = f'{tier} {size}x{size}'
            previous = read(name, 'client').get('menu_id')
            (control / f'{name}-open.json').write_text(json.dumps(dict(tier=tier, size=size)))
            wait(lambda: read(name, 'client').get('menu_id') != previous
                 and read(name, 'client').get('size') == size
                 and read(name, 'client').get('capacity') == {'leather': 27, 'netherite': 78, 'large': 117}[tier], process, 'crafting backpack opens')
            act('register')
            wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'storage registration closes menu')
            previous = read(name, 'client').get('menu_id')
            (control / f'{name}-open.json').write_text(json.dumps(dict(tier=tier, size=size)))
            wait(lambda: read(name, 'client').get('menu_id') != previous
                 and read(name, 'client').get('size') == size, process, 'registered crafting backpack reopens')
            act('resize', width=640, height=400)
            act('book-state', open=False)
            settle()
            validation = act('validate')
            check(validation['filtered_grid_and_result_excluded'], label + ': crafting panel excluded from storage classification')
            if not baseline:
                reference = act('button-reference')
                button_offsets.append(dict(menu=label, tier=tier, offsets=reference['inventory_button_offsets'], container_offsets=reference['container_button_offsets']))

            if not baseline and not placement_only:
                for icon, shift in cases:
                    prepare('storage-reset')
                    before = read(name, 'server')
                    action = act('storage-operation', icon='sorted:widget/' + icon, shift=shift)
                    settle()
                    wait(lambda: read(name, 'server').get('cursor') == 0
                         and read(name, 'client').get('cursor') == 0
                         and totals(read(name, 'server')) == totals(before)
                         and read(name, 'server').get('holder') == read(name, 'server').get('storage'),
                         process, 'authoritative transfer acknowledgements converge')
                    after = read(name, 'server')
                    context = label + ': ' + ('Shift ' if shift else '') + icon
                    check(action['client_crafting_before'] == action['client_crafting_after'], context + ' preserves exact client crafting contents', action)
                    check((before['grid'], before['result']) == (after['grid'], after['result']), context + ' preserves authoritative grid and output', after)
                    check(totals(before) == totals(after), context + ' conserves all storage and player items', after)
                    before_player = [stack for index, stack in enumerate(before['inventory_slots']) if index not in (12, 13)]
                    after_player = [stack for index, stack in enumerate(after['inventory_slots']) if index not in (12, 13)]
                    check((before['storage_slots'], before_player) != (after['storage_slots'], after_player), context + ' actually sorts or transfers seeded items', after)
                    check(after['cursor'] == 0 and read(name, 'client')['cursor'] == 0, context + ' leaves no held leftovers')
                    check(after['holder'] == after['storage'], context + ' synchronizes backpack contents')
                    if icon == 'quick_empty' and not shift:
                        check(after['inventory_slots'][:2] == before['inventory_slots'][:2], context + ' preserves hotbar exclusion')
                    if icon == 'quick_empty' and shift:
                        check(after['inventory'].get('minecraft:cobblestone', 0) == 0
                              and after['inventory'].get('minecraft:oak_planks', 0) == 0, context + ' includes eligible hotbar sources')
                    operations.append(dict(tier=tier, grid=size, icon=icon, shift=shift, action=action, before=before, after=after))

            for scenario in ([] if placement_only else blocked if baseline else scenarios):
                prepare('recovery-reset', scenario=scenario)
                before = read(name, 'server')
                action = act('storage-recovery', scenario=scenario, origin=before['recovery_origin'],
                             expected_target=before['expected_recovery_target'], baseline=baseline)
                after = read(name, 'server')
                context = label + ': recovery ' + scenario
                check(action['actual_private_selector_invoked'], context + ' calls the original recovery selector')
                check(action['target_is_crafting'] if baseline else not action['target_is_crafting'], context + (' reproduces published grid leak' if baseline else ' excludes crafting inputs and output'), action)
                check(action['all_slots_unchanged'] and action['cursor_unchanged'], context + ' selector leaves actual item state unchanged')
                check((before['grid'], before['result'], before['storage'], before['inventory'], before['cursor'])
                      == (after['grid'], after['result'], after['storage'], after['inventory'], after['cursor']), context + ' preserves authoritative item state')
                recovery.append(dict(tier=tier, grid=size, scenario=scenario, action=action))

            prepare('storage-reset')
            act('screenshot', name=f'storage-actions-{tier}-{size}')
            act('close')
            wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'storage fixture closes')

    normal_reference = None
    plain_backpack_references = []
    if not baseline:
        act('backpack-size', restore=True)
        previous = read(name, 'client').get('menu_id')
        (control / f'{name}-open.json').write_text(json.dumps(dict(reference=True, tier='leather', size=0)))
        wait(lambda: read(name, 'client').get('menu_id') != previous
             and read(name, 'client').get('capacity') == 27
             and read(name, 'server').get('menu') == 'net.minecraft.world.inventory.ChestMenu', process, 'normal chest reference opens')
        act('resize', width=640, height=400)
        settle()
        normal_reference = act('button-reference')
        act('screenshot', name='normal-chest-control-reference')
        act('close')
        wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'normal reference closes')
        for tier in ('leather', 'netherite', 'large'):
            act('backpack-size', rows=9 if tier == 'large' else 6, columns=13)
            capacity = {'leather': 27, 'netherite': 78, 'large': 117}[tier]
            for registered in (False, True):
                previous = read(name, 'client').get('menu_id')
                (control / f'{name}-open.json').write_text(json.dumps(dict(tier=tier, size=0)))
                wait(lambda: read(name, 'client').get('menu_id') != previous
                     and read(name, 'client').get('capacity') == capacity
                     and read(name, 'server').get('menu') == 'me.pajic.tiered_backpacks.ui.BackpackMenu', process, 'original backpack reference opens')
                if not registered:
                    act('register')
                    wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'plain backpack registration closes')
            act('resize', width=640, height=400)
            settle()
            reference = act('button-reference')
            for row in (row for row in button_offsets if row['tier'] == tier):
                check(row['offsets'] == reference['inventory_button_offsets'],
                      row['menu'] + ': inventory controls match original full-frame placement', row)
                check(row['container_offsets'] == reference['container_button_offsets'],
                      row['menu'] + ': backpack controls match original above-panel right alignment', row)
            plain_backpack_references.append(dict(tier=tier, capacity=capacity, reference=reference))
            act('screenshot', name=f'original-backpack-control-reference-{tier}')
            act('close')
            wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'original backpack reference closes')
        restored_size = act('backpack-size', restore=True)
        check(restored_size['original_size_restored'], 'Original client backpack dimensions restored')
        (control / f'{name}-open.json').write_text(json.dumps(dict(reference=True, tier='leather', size=0)))
        wait(lambda: read(name, 'server').get('menu') == 'net.minecraft.world.inventory.ChestMenu', process, 'server dimensions restore')
        act('close')
        wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'restoration reference closes')

    return dict(profile='suite', assertion_count=len(checks), checks=checks, operations=operations,
                recovery=recovery, button_offsets=button_offsets, normal_reference=normal_reference,
                plain_backpack_references=plain_backpack_references, baseline=baseline, placement_only=placement_only,
                scope='Actual Sorted widget callbacks with matching crafting ingredients, plus direct original cursor-recovery selection. Recovery selection is tested without simulating a full interrupted queue.')
