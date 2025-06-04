# City Note GMap AI

## Description
This repo is based on the finished code for the [Google Maps Platform 101: React codelab](https://developers.google.com/codelabs/maps-platform/maps-platform-101-react-js).

The project aims to develop a city-based travel recommendation app, providing users with a map-based travel note community, multi-type note uploading, and AI-powered auto travel tip drafting.

## Techstack architecture

This project is developed using React (TypeScript) as frontend, Spring Boot as backend, and MySQL / MangoDB as database. The backend and database are deployed on AWS EC2 virtual machine for public access.

## Getting Started

To run the app, run the following from their respective directories:

1. `npm i`
2. `npm start`

This will install the needed dependencies and run the app locally in your browser using Webpack Dev Server.

## Google Map API 使用

首先在 `Google Map API` 中激活自己的 `API Key` ，然后在 `package.json` 的同级目录中 新建 `env.js` ，定义两个变量 export const `GMAP_API_KEY` 和 `GMAP_MAP_ID` ，定义并赋值之后 `app.tsx` 会自动引用，然后地图部分的功能应该就可以正常跑起来了


## 数据库设置
1. 确保MySQL已安装并运行
2. 复制项目根目录的`schema.sql`到本地
3. 运行命令初始化数据库：
```bash
mysql -u root -p < schema.sql
```

## 运行后端
1. 进入backend目录
2. 运行命令：
```bash
./mvnw spring-boot:run
```
