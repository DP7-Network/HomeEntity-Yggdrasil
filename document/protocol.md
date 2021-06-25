## HomeEntity Yggdrasil 协议定义

- [HomeEntity Yggdrasil 协议定义](#homeentity-yggdrasil-----)
  * [数据类型定义](#数据类型定义)
  * [Mojang Yggdrasil Protocol](#mojang-yggdrasil-protocol)
    + [/authenticate](#-authenticate)
    + [!! /refresh](#----refresh)
    + [!! /validate](#----validate)
    + [!! /logout](#----logout)
    + [!! /invalidate](#----invalidate)
  * [HomeEntity Yggdrasil Web Protocol](#homeentity-yggdrasil-web-protocol)
    + [!! /management/login](#----management-login)
    + [!! /management/changePassword](#----management-changepassword)
    + [!! /management/setSkin](#----management-setskin)
    + [!! /management/setCape](#----management-setcape)
    + [!! /management/register](#----management-register)
    + [!! /management/online](#----management-online)

---
### 数据类型定义

| 符号 | 定义 | 区域 | 格式 | 例子 |
| :---- | :---- | :---- | :---- | :---- |
| `<-` | 类型标识符，确定某个值的类型 | 全局 | `名字 <- 类型` | `name <- string` |
| `?` | 可空标识符，当类型后存在此符号时，值代表可能为`null` | 全局 | `类型?` | `name <- string?` |
| `<*, ...>` | 泛型标识符，当类型后存在此符号表示此类型可泛型 | 定义区 | `类型<*>` | `names <- list<*>` |
| `<* <- *, ...>` | 泛型范围规定符，规定泛型的继承范围 | 定义区 | `类型A<* <- 类型B>` | `names <- list<* <- string>` |
| `<...>` | 泛型类型符，确定泛型类型 | 接口区 | `类型A<类型B>` | `names <- list<string>` |
| `=` | 定值符，确定该值应永远为`=`后内容 | 全局 | `名字 <- 类型 = 值` | `name <- string = lama` |
| `!!` | 在接口名称前若有!!代表该接口没有异常处理，若返回 `500 Internal Server Error` 代表您请求内容可能含有非法内容或服务器Bug | 接口定义区 | `!! 接口名称` | `!! /management/login` |

- 类型定义: 
   - `bool`: 布尔类型，取值范围: `true` / `false`
   - `int`: 整数类型，取值范围: 0x7FFFFFFF (2^31-1) ~ -0x80000000
   - `string`: 文本类型，取值范围: 任意字符串
   - `list`: 列表类型，取值范围：任意
   - MojangYggdrasilAgent: 
      ```
       { 
         name <- string
         version <- int
       }
      ```
   - AvailableProfile:
      ```
        {
          id <- string
          name <- string
        }
      ```
   - SelectedProfile:
      ```
        {
          id <- string
          name <- string
        }
      ```
  - MojangProperty:
    ```
      {
        name <- string
        value <- string
      }
    ```
  - MojangUser:
    ```
      {
        id <- string
        properties <- list<MojangProperty>
      }
    ```


---   
### Mojang Yggdrasil Protocol

#### /authenticate
- 内容: 游戏客户端登录
- 请求方法: `POST`
- 请求头: 
   + Content-Type -> application/json
- 请求类型: `Json`
- 请求内容: 
   ```
    { 
      // 请保持 MojangYggdrasilAgent 中name属性永远等于minecraft
      agent <- MojangYggdrasilAgent, 
      // 客户端识别符，随意填写或填写 null
      clientToken <- string?
      // 密码
      password <- string
      requestUser <- bool = true
      // 登录用户名
      username <- string 
    }
   ```
- 正常返回: 
  - Http返回码: `200 OK`
     - 内容类型: `Json`
     - 内容格式:
       ```
        {
          // 访问识别符，登录客户端使用
          accessToken <- string
          // 可用用户档案
          availableProfiles <- list<AvailableProfile>
          // 你的客户都标识符，如果你传入了此数据，此处应和你传入的一致，否则随机产生
          clientToken <- string
          // 已选择的用户档案
          selectedProfile <- SelectedProfile
        }
       ```
- 异常返回:
   - Http返回码: `403 Forbidden`
     - 原因: 用户名/密码不正确
     - 内容类型: `Json`
     - 内容格式:
       ```
         {
            error <- string
            errorMessage <- string
            cause <- string
         }
       ```
  - Http返回码: `406 Not Acceptable`
    - 原因: 不支持除了 minecraft 以外的游戏
    - 内容类型: `Json`
    - 内容格式:
      ```
        {
           error <- string
           errorMessage <- string
           cause <- string
        }
      ```
  - Http返回码: `400 Bad Request`
    - 原因: 不支持除了标准Json以外任何类型 或 请求头中没有(不是)`Content-Type: application/json` 具体请留意errorMessage
    - 内容类型: `Json`
    - 内容格式:
      ```
        {
           error <- string
           errorMessage <- string
           cause <- string
        }
      ```
#### !! /refresh
- 内容: 刷新访问识别符
- 请求方法: `POST`
- 请求头:
  + Content-Type -> application/json
- 请求格式: `Json`
- 请求内容:
   ```
    { 
      // 客户端识别符
      clientToken <- string
      // 刷新访问识别符
      accessToken <- string
      requestUser <- bool = true
      // 选择的用户档案
      selectedProfile <- SelectedProfile
    }
   ```
- 正常返回:
  - Http返回码: `200 OK`
  - 内容类型: `Json`
  - 内容格式:
    ```
      {
        // 访问识别符，登录客户端使用
        accessToken <- string
        // 可用用户档案
        availableProfiles <- list<AvailableProfile>
        // 你的客户都标识符，如果你传入了此数据，此处应和你传入的一致，否则随机产生
        clientToken <- string
        // 已选择的用户档案
        selectedProfile <- SelectedProfile
      }
    ```
- 异常返回:
  - Http返回码: `403 Forbidden`
    - 原因: 访问识别符不正确或不存在
  - Http返回码: `400 Bad Request`
    - 原因: 请求头中没有(不是)`Content-Type: application/json`
    - 内容类型: `Json`
    - 内容格式:
      ```
        {
           error <- string
           errorMessage <- string
           cause <- string
        }
      ```
      
#### !! /validate
- 内容: 验证访问识别符
- 请求方法: `POST`
- 请求头:
  + Content-Type -> application/json
- 请求类型: `Json`
- 请求内容:
   ```
    { 
      // 客户端识别符
      clientToken <- string
      // 刷新访问识别符
      accessToken <- string
    }
   ```
- 正常返回:
  - Http返回码: `204 No Content`
- 异常返回:
  - Http返回码: `403 Forbidden`
    - 原因: 访问识别符不正确或不存在
  - Http返回码: `400 Bad Request`
    - 原因: 请求头中没有(不是)`Content-Type: application/json`
    - 内容类型: `Json`
    - 内容格式:
      ```
        {
           error <- string
           errorMessage <- string
           cause <- string
        }
      ```
      
#### !! /logout
- 内容: 使用账号密码退出登录，使得访问识别符不可用
- 请求方法: `POST`
- 请求头:
  + Content-Type -> application/json
- 请求类型: `Json`
- 请求内容:
   ```
    { 
      password <- string
      username <- string 
    }
   ```
- 正常返回:
  - Http返回码: `200 OK`
- 异常返回:
  - Http返回码: `403 Forbidden`
    - 原因: 账号/密码错误
  
#### !! /invalidate
- 内容: 注销访问识别符
- 请求方法: `POST`
- 请求头:
  + Content-Type -> application/json
- 请求类型: `Json`
- 请求内容:
   ```
    { 
      // 客户端识别符
      clientToken <- string
      // 访问识别符
      accessToken <- string
    }
   ```
- 正常返回:
  - Http返回码: `204 No Content`
- 异常返回:
  - Http返回码: `403 Forbidden`
    - 原因: 访问识别符不正确或不存在

---
### HomeEntity Yggdrasil Web Protocol

#### !! /management/login
- 内容: 网页登录
- 请求方法: `POST`
- 请求头:
  + Content-Type -> application/json
- 请求类型: `Json`
- 请求内容:
   ```
    { 
      password <- string
      username <- string 
    }
   ```
- 正常返回:
  - Http返回码: `204 No Content`
  - Set-Cookie:
     - Token: <访问识别符>
- 异常返回:
  - Http返回码: `403 Forbidden`
    - 原因: 账号/密码错误
  
#### !! /management/changePassword
- 内容: 修改密码
- 请求方法: `POST`
- 请求头:
  + Content-Type -> application/json
  + Cookie -> 登录中获取到的访问识别符
- 请求类型: `Json`
- 请求内容:
   ```
    { 
      old <- string
      new <- string 
    }
   ```
- 正常返回:
  - Http返回码: `204 No Content`
- 异常返回:
  - Http返回码: `403 Forbidden`
    - 原因: 老密码不正确
  - Http返回码: `401 Unauthorized`
    - 原因: 未携带Cookie或Cookie值不正确

#### !! /management/setSkin
- 内容: 设置皮肤
- 请求方法: `POST`
- 请求头:
  + Content-Type -> application/json
  + Cookie -> 登录中获取到的访问识别符
- 请求类型: `Json`
- 请求内容:
   ```
    { 
      url <- string 
    }
   ```
- 正常返回:
  - Http返回码: `204 No Content`
- 异常返回:
  - Http返回码: `500 Internal Server Error`
    - 原因: 内部错误，联系开发者解决
  - Http返回码: `401 Unauthorized`
    - 原因: 未携带Cookie或Cookie值不正确

#### !! /management/setCape
- 内容: 设置披风
- 请求方法: `POST`
- 请求头:
  + Content-Type -> application/json
  + Cookie -> 登录中获取到的访问识别符
- 请求类型: `Json`
- 请求内容:
   ```
    { 
      url <- string 
    }
   ```
- 正常返回:
  - Http返回码: `204 No Content`
- 异常返回:
  - Http返回码: `500 Internal Server Error`
    - 原因: 内部错误，联系开发者解决
  - Http返回码: `401 Unauthorized`
    - 原因: 未携带Cookie或Cookie值不正确

#### !! /management/register
- 内容: 注册
- 请求方法: `POST`
- 请求头:
  + Content-Type -> application/json
- 请求类型: `Json`
- 请求内容:
   ```
    { 
      password <- string
      username <- string 
    }
   ```
- 正常返回:
  - Http返回码: `204 No Content`
  - Set-Cookie:
- 异常返回:
  - Http返回码: `400 Bad Request`
    - 原因: 请求格式不正确
  - Http返回码: `200 OK`
    - 原因: 账号已被注册

#### !! /management/online
- 内容: 列出在线人员列表
- 请求方法: `GET`
- 正常返回:
  - Http返回码: `200 OK`
    - 内容类型: `Json`
  - 内容格式:
    ```
      {
        // 在线人数
        count <- int
        // 在线人数名单
        names <- list<string>
      }
    ```
