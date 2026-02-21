-- Initial seed data for SC2 StarFinder (MySQL compatible)
-- Uses INSERT IGNORE to avoid duplicates on restart

-- Sample users (password is '123456')
INSERT IGNORE INTO
    users (
        id,
        name,
        battle_tag,
        character_id,
        race,
        mmr,
        email,
        password,
        qq,
        stream_url,
        signature,
        region,
        role
    )
VALUES (
        1,
        'Amaris',
        'Amaris#31262',
        12345,
        'T',
        4249,
        '12345',
        '12345',
        '12345',
        '',
        '星际争霸2爱好者',
        'EU',
        'admin'
    );

INSERT IGNORE INTO
    users (
        id,
        name,
        battle_tag,
        character_id,
        race,
        mmr,
        email,
        password,
        qq,
        stream_url,
        signature,
        region,
        role
    )
VALUES (
        2,
        'Player2',
        'Player2#1234',
        NULL,
        'Z',
        4100,
        '13800000002',
        '123456',
        '100000002',
        'https://www.bilibili.com/example',
        '虫群之心',
        'US',
        'user'
    );

INSERT IGNORE INTO
    users (
        id,
        name,
        battle_tag,
        character_id,
        race,
        mmr,
        email,
        password,
        qq,
        stream_url,
        signature,
        region,
        role
    )
VALUES (
        3,
        'Player3',
        'Player3#5678',
        NULL,
        'P',
        4300,
        '13800000003',
        '123456',
        '100000003',
        '',
        '我的荣耀即忠诚',
        'US',
        'user'
    );

INSERT IGNORE INTO
    users (
        id,
        name,
        battle_tag,
        character_id,
        race,
        mmr,
        email,
        password,
        qq,
        stream_url,
        signature,
        region,
        role
    )
VALUES (
        4,
        'Player4',
        'Player4#9999',
        NULL,
        'T',
        3800,
        '13800000004',
        '123456',
        '100000004',
        '',
        'GG',
        'KR',
        'user'
    );

INSERT IGNORE INTO
    users (
        id,
        name,
        battle_tag,
        character_id,
        race,
        mmr,
        email,
        password,
        qq,
        stream_url,
        signature,
        region,
        role
    )
VALUES (
        5,
        'Player5',
        'Player5#1111',
        NULL,
        'Z',
        4500,
        '13800000005',
        '123456',
        '100000005',
        '',
        '运营流',
        'EU',
        'user'
    );

-- Sample cheaters
INSERT IGNORE INTO
    cheaters (
        id,
        battle_tag,
        cheat_type,
        description,
        reported_by,
        status,
        mmr,
        race
    )
VALUES (
        1,
        'Hacker1#1234',
        '开图',
        '多次被发现开图行为，已有录像证据',
        1,
        'approved',
        4200,
        'T'
    );

INSERT IGNORE INTO
    cheaters (
        id,
        battle_tag,
        cheat_type,
        description,
        reported_by,
        status,
        mmr,
        race
    )
VALUES (
        2,
        'Hacker2#5678',
        '建造列表',
        '使用第三方建造列表插件',
        1,
        'approved',
        3900,
        'P'
    );

-- Sample tutorials
INSERT IGNORE INTO
    tutorials (
        id,
        title,
        url,
        category,
        description,
        author
    )
VALUES (
        1,
        '[瑞白]PVT闪追巨像',
        'https://www.bilibili.com/video/BV1example1',
        'PvT',
        '星灵对人族的闪追巨像打法教学',
        'Amaris'
    );

INSERT IGNORE INTO
    tutorials (
        id,
        title,
        url,
        category,
        description,
        author
    )
VALUES (
        2,
        '[瑞白]ZVP运营教学',
        'https://www.bilibili.com/video/BV1example2',
        'ZvP',
        '异虫对星灵的运营思路详解',
        'Amaris'
    );

INSERT IGNORE INTO
    tutorials (
        id,
        title,
        url,
        category,
        description,
        author
    )
VALUES (
        3,
        'TvZ生化打法入门',
        'https://www.bilibili.com/video/BV1example3',
        'TvZ',
        '人族对异虫生化流派基础教学',
        'Amaris'
    );

-- Sample events
INSERT IGNORE INTO
    events (
        id,
        title,
        description,
        rules,
        rewards,
        contact_link,
        group_link,
        submitted_by,
        status,
        region,
        start_time
    )
VALUES (
        1,
        'SC2 周末杯赛',
        '每周六晚8点开始的业余杯赛，欢迎所有水平的玩家参加！',
        'Bo3单败淘汰赛，禁图：金色城墙',
        '冠军奖金100元',
        '',
        'https://qm.qq.com/example',
        1,
        'approved',
        'CN',
        '2026-02-22 20:00'
    );

INSERT IGNORE INTO
    events (
        id,
        title,
        description,
        rules,
        rewards,
        contact_link,
        group_link,
        submitted_by,
        status,
        region,
        start_time
    )
VALUES (
        2,
        '星际2高手挑战赛',
        'MMR 4000+玩家专属赛事',
        'Bo5双败淘汰赛',
        '冠军奖金500元 + 定制周边',
        '',
        'https://qm.qq.com/example2',
        1,
        'approved',
        'CN',
        '2026-03-01 19:00'
    );