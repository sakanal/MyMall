import axios from "axios";
import cookie from "js-cookie";
import {Message} from "element-ui";

const service = axios.create({
  baseURL: 'http://localhost:9527'
})

// http request 拦截器
service.interceptors.request.use(
  config => {
    // debugger
    //判断cookie中是否有名称叫token的数据
    if (cookie.get('token')) {
      //把获取到的cookie值放到header中
      config.headers['token'] = cookie.get('token');
    }
    return config
  },
  err => {
    return Promise.reject(err);
  }
)

// http response 拦截器
service.interceptors.response.use(
  response => {
    //debugger
    if (response.data.code === 28004) {
      console.log("response.data.resultCode是28004")
      // 返回 错误代码-1 清除ticket信息并跳转到登录页面
      //debugger
      window.location.href = "/login"

    } else {
      if (response.data.code !== 20000) {
        //25000：订单支付中，不做任何提示
        if (response.data.code !== 25000) {
          Message({
            message: response.data.message || 'error',
            type: 'error',
            duration: 5 * 1000
          })
        }
      } else {
        return response;
      }
    }
  },
  error => {
    return Promise.reject(error.response) // 返回接口返回的错误信息
  }
)

export default service
