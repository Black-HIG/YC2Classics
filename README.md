# YC2Classics

A server backend for some article storage

---

# Build and run
## Docker Compose

1. `git clone https://github.com/ZZSZ-YCT/YC2Classics.git`
2. `cp example.env .env`
3. Edit `.env` file as you want
4. `docker compose up`

## Gradle

1. Edit your environment variable according to `example.env`
2. `gradle runFatJar`

---

# Using

You can find the initial admin password in console

---

# TODO

1. Tags and Searching
2. Finish README.MD
3. 1
4. 3
5. 4
6. 允许置顶,即json传的时候可以传"is_pin"参数来确认是否置顶
7. 适配空贡献者,因为我的html适配过了
8. 加密部分:允许对单条/全站进行加密,全站加密就是打开需要密码,但是是html实现的密码,不是nginx的实现,类似于登录,适配微信客户端,类似于aops,如果超过部分时间没有任何操作,直接恢复需要输入密码的状态,,单条加密就是在json中传入,注意,注意,这些密码都需要加密后储存,然后验证是否正确
9. 允许对内容进行分类,json中传入参数"category",但是不能凭空创建分类,创建分类需要jwt的权限