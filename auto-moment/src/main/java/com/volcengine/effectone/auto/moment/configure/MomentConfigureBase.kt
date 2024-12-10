package com.volcengine.effectone.auto.moment.configure

object Constants {
    val MOMENT_ALL_TAGS : List<String> = listOf(
        "云",
        "蓝天白云",
        "火烧云",
        "月亮",
        "星空（银河/星轨/极光）",
        "彩虹",
        "花",
        "花海",
        "园林",
        "草地",
        "农田",
        "山溪",
        "飞机",
        "马路",
        "儿童",
        "单人",
        "双人",
        "吉他",
        "向日葵",
        "商场",
        "多人",
        "夜景",
        "小岛",
        "摩天轮",
        "摩托车",
        "晚会",
        "栈道",
        "树",
        "桥",
        "梯田",
        "森林",
        "池塘",
        "汽车",
        "沙滩",
        "海",
        "湖",
        "演唱会",
        "火",
        "烟花",
        "礁石",
        "老人",
        "自行车",
        "火车",
        "船舶",
        "车流",
        "街道",
        "乌云",
        "闪电",
        "雨天",
        "骑行",
        "雪景",
        "雾",
        "霓虹灯",
        "露营",
        "高铁",
        "郁金香",
    )

    val MOMENT_ALL_LOCATION : List<String> = listOf(
        "北京",
        "上海",
        "广州",
        "深圳",
    )

    const val MOMENT_CONFIGURE_ROOT_PATH = "moment_configs"

    // moment 配置文件的默认路径
    const val MOMENT_DEFAULT_CONFIG_ASSET_PATH = "moment_default_config"

    // moment 配置文件的JSON默认名称
    const val MOMENT_DEFAULT_CONFIG_ASSET_JSON_NAME = "default_config.json"

    const val MOMENT_CONFIDENCE_CHANGE_STEP = 0.1F
}