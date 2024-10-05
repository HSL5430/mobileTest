1. 实现-个data manager 充当booking的数据提供者，源数据参考附件中的booking.json 
    1. 包含service层
    2. 包含本地持久化缓存层
    3. 持久化缓存层数据过期时间为5分钟
    4. 支持自动刷新机制和提供对外统一接口获取数据
        1. 有本地缓存，先返回缓存(无论是否过期)
        2. 数据过期后，需自动触发刷新机制获取新的api response 替换旧有数据，并返回最新数据
    5. 错误处理
2. 创建blank demo page,要求该page每次出现(不一定是首次创建)的时候，调用data provider 接口，在console中打印出对应data。
3. 注意事项
    1. service 层可以使用提供的.json 文件 mock response.
    2. 使用git commit 代码，上传至github,仓库名统一改名为"mobileTest"