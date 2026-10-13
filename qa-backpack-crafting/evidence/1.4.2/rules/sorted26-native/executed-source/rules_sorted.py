"""Rule editing and visible marker checks through original Sorted callbacks."""
import json


def run_rules_checks(name, process, read, wait, control):
    checks, cases = [], []
    action_id = request_id = 0

    def act(action_type, **values):
        nonlocal action_id
        action_id += 1
        (control / f'{name}-action.json').write_text(json.dumps(dict(type=action_type, id=action_id, **values)))
        wait(lambda: any(row.get('id') == action_id for row in read(name, 'client').get('actions', [])), process, action_type)
        return next(row for row in read(name, 'client')['actions'] if row['id'] == action_id)

    def settle():
        tick = read(name, 'client')['ticks']
        wait(lambda: read(name, 'client').get('ticks', 0) >= tick + 15, process, 'rule callback packets settle')

    def reset(size):
        nonlocal request_id
        request_id += 1
        (control / f'{name}-layout-server-request.json').write_text(json.dumps(dict(type='reset', id=request_id)))
        wait(lambda: read(name, 'server').get('prepared_id') == request_id
             and read(name, 'client').get('cursor') == 0
             and (not size or read(name, 'client').get('grid_count') == 2
                  and read(name, 'client').get('result_count') == 4), process, 'fresh authoritative rule fixture')
        settle()

    try:
        for tier in ('leather', 'netherite'):
            for size in (0, 2, 3):
                previous = read(name, 'client').get('menu_id')
                (control / f'{name}-open.json').write_text(json.dumps(dict(tier=tier, size=size)))
                wait(lambda: read(name, 'client').get('menu_id') != previous
                     and read(name, 'client').get('size') == size
                     and read(name, 'client').get('capacity') == (27 if tier == 'leather' else 78), process, 'rule backpack opens')
                act('setup')
                settle()
                edits = []
                if size:
                    for target in ('grid', 'result'):
                        for kind in ('left', 'right', 'scroll'):
                            reset(size)
                            edit = act('edit', target=target, kind=kind, excluded=True)
                            assert edit['global_config_unchanged'] and edit['saved_config_unchanged'], edit
                            checks.extend([f'{tier} {size} {target} Alt-{kind}: global configuration unchanged',
                                           f'{tier} {size} {target} Alt-{kind}: saved sorted.json unchanged'])
                            edits.append(edit)
                            settle()
                for target in ('storage', 'player'):
                    for kind in ('left', 'right', 'scroll'):
                        reset(size)
                        edit = act('edit', target=target, kind=kind, excluded=False)
                        assert edit['only_target_rule_changed'], edit
                        assert edit['keep_after'] == (0 if kind == 'left' else 2), edit
                        checks.extend([f'{tier} {size or "plain"} {target} Alt-{kind}: original item rule remains editable',
                                       f'{tier} {size or "plain"} {target} Alt-{kind}: unrelated rules/settings preserved and exact change saved'])
                        edits.append(edit)
                        settle()
                reset(size)
                markers = act('markers')
                counts = markers['marker_sprite_counts']
                assert counts['storage'] > 0 and counts['player'] > 0, counts
                checks.extend([f'{tier} {size or "plain"}: actual storage keep markers render',
                               f'{tier} {size or "plain"}: actual player keep markers render'])
                if size:
                    assert counts['grid'] == 0 and counts['result'] == 0, counts
                    checks.extend([f'{tier} {size}: actual grid markers excluded with a matching global rule',
                                   f'{tier} {size}: actual output markers excluded with a matching global rule'])
                settle()
                act('screenshot', name=f'keep-rules-{tier}-{size or "plain"}')
                restored = act('restore')
                assert restored['original_config_restored'], restored
                checks.append(f'{tier} {size or "plain"}: original global configuration fully restored')
                act('close')
                wait(lambda: read(name, 'server').get('menu_id') == 0, process, 'rule fixture closes')
                cases.append(dict(tier=tier, grid=size, edits=edits, marker_render=markers))
    finally:
        if read(name, 'client').get('ticks', 0) and process.poll() is None:
            act('restore')
    return dict(profile='suite', assertion_count=len(checks), cases=cases,
                scope='Actual Alt-left/right/scroll callbacks, full memory and saved global-config preservation for grid/output, storage/player editing, original slot marker renderer and full settings restoration. Vanilla grid pickup/crafting remains allowed.')
