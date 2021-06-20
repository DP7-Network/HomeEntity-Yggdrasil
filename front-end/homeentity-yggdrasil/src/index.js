import React from 'react'
import ReactDOM from 'react-dom'
import { FXAASkinViewer } from 'skinview3d'
import './index.css'

function Main() {
  return (
    <div className="flex h-screen">
    
      <div className="w-1/4 m-auto border-2 border-gray-300 rounded-2xl">
        
        <div className="px-4 pt-4 mb-4">
          <label className="block mb-2 text-sm font-bold text-center text-gray-700">
            Welcome to DP7 Charmless Server!
          </label>
        </div>
      
        <div className="px-4 mb-4">
          <label className="block mb-2 text-sm font-bold text-gray-700">
            Username
          </label>
          <input className="w-full px-3 py-2 leading-tight text-gray-700 border rounded shadow appearance-none focus:outline-none focus:shadow-outline" id="username" type="text" placeholder="Username" />
        </div>
        
        <div className="px-4">
          <label className="block mb-2 text-sm font-bold text-gray-700">
            Password
          </label>
          <input className="w-full px-3 py-2 mb-3 leading-tight text-gray-700 border rounded shadow appearance-none focus:outline-none focus:shadow-outline" id="password" type="password" placeholder="******************" />
        </div>
          
        
        <div className="flex items-center px-6 pt-2 pb-4">
          <button className="px-4 py-2 m-auto font-bold text-white bg-blue-500 rounded hover:bg-blue-700 focus:outline-none focus:shadow-outline" type="button" onClick={() => {
            ReactDOM.unmountComponentAtNode(document.getElementById('root'))
            ReactDOM.render(<PlayerInfoPage userData={{skin: "https://texture.namemc.com/1f/07/1f0736f6dca98a77.png", name: "Lama3L9R", uuid: "abcde-fghij-klmno-pqrst", }}/>, document.getElementById('root'))
          }}>
            Sign In
          </button>
          
          <button className="px-4 py-2 m-auto font-bold text-white bg-blue-500 rounded hover:bg-blue-700 focus:outline-none focus:shadow-outline" type="button">
            Register
          </button>
        </div>
      </div>
    </div>
  )
}
//?
function PlayerInfoPage({userData}) {
  return(
    <div className="flex h-screen">
      <div className="flex flex-row w-1/3 m-auto border-2 border-gray-300 rounded-2xl">
        <div className="border-2 border-gray-300">
          <canvas id="skinView" ref={ canvas => {
            let skinViewer = new FXAASkinViewer({
              alpha: true,
              canvas: document.getElementById('skinView'),
              skin: userData.skin,
              cape: userData.cape,
              width: 300,
              height: 400
            })
          }}/>
        </div>
        <div className="flex flex-col">
          <a>用户名: {userData.name}</a>
          <a>UUID: {userData.uuid}</a>
          <div className="flex">
            <a>修改密码: </a>
            <input className="w-full px-3 py-2 leading-tight text-gray-700 border rounded shadow appearance-none focus:outline-none focus:shadow-outline" type="password"/>
          </div>
          <div className="flex flex-row">
            <input className="px-4 py-2 m-auto font-bold text-white bg-blue-500 rounded hover:bg-blue-700 focus:outline-none focus:shadow-outline" type="button" value={"修改密码"} />
            <input className="px-4 py-2 m-auto font-bold text-white bg-blue-500 rounded hover:bg-blue-700 focus:outline-none focus:shadow-outline" type="button" value={"更换皮肤"} />
          </div>
        </div>
      </div>
    </div>
  )
}

ReactDOM.render(<Main />, document.getElementById('root'))
