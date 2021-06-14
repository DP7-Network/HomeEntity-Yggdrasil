import React from 'react'
import ReactDOM from 'react-dom'
import './index.css'

function Main() {
  return (
    <div className="flex h-screen">
    
      <div className="w-1/4 border-gray-300 border-2 rounded-2xl m-auto">
        
        <div className="mb-4 px-4 pt-4">
          <label className="block text-center text-gray-700 text-sm font-bold mb-2">
            Welcome to DP7 Charmless Server!
          </label>
        </div>
      
        <div className="mb-4 px-4">
          <label className="block text-gray-700 text-sm font-bold mb-2">
            Username
          </label>
          <input className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" id="username" type="text" placeholder="Username" />
        </div>
        
        <div className="px-4">
          <label className="block text-gray-700 text-sm font-bold mb-2">
            Password
          </label>
          <input className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline" id="password" type="password" placeholder="******************" />
        </div>
          
        
        <div className="flex items-center pb-4 pt-2 px-6">
          <button className="bg-blue-500 hover:bg-blue-700 text-white m-auto font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline" type="button" onClick={() => {
            ReactDOM.unmountComponentAtNode(document.getElementById('root'))
            ReactDOM.render(<PlayerInfoPage />, document.getElementById('root'))
          }}>
            Sign In
          </button>
          
          <button className="bg-blue-500 hover:bg-blue-700 text-white m-auto font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline" type="button">
            Register
          </button>
        </div>
      </div>
    </div>
  )
}

function PlayerInfoPage() {
  return(
    <div className="flex h-screen">
      <div className="flex flex-row w-1/3 border-gray-300 border-2 rounded-2xl m-auto">
        <div className="border-gray-300 border-2">
          <a> 占用.jpg</a>
        </div>
        <div className="flex flex-col">
          <a>用户名: {"Lama3L9R"}</a>
          <a>UUID: {"ABCDE-FGHIJ-KMLNO-PQRST-UVWXY"}</a>
          <div className="flex">
            <a>修改密码: </a>
            <input className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" type="password"/>
          </div>
          <div className="flex flex-row">
            <input className="bg-blue-500 hover:bg-blue-700 text-white m-auto font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline" type="button" value={"修改密码"} />
            <input className="bg-blue-500 hover:bg-blue-700 text-white m-auto font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline" type="button" value={"更换皮肤"} />
          </div>
        </div>
      </div>
    </div>
  )
}

ReactDOM.render(<Main />, document.getElementById('root'))
