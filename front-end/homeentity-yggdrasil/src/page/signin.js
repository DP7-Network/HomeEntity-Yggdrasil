/* eslint-disable jsx-a11y/anchor-is-valid */
/* eslint-disable react/jsx-no-comment-textnodes */
import React from "react";
import ReactDOM from "react-dom";
import i18n from "../i18n/";
import PlayerInfoPage from "../components/PlayerInfo";
import config from "../config.js";

const context = i18n();

const click = (element) => {
  signIn();
}

const signIn = () => {
  alert("Sign in");
  ReactDOM.unmountComponentAtNode(document.getElementById("root"));
  ReactDOM.render(
    <PlayerInfoPage
      userData={{
        skin:
          "https://texture.namemc.com/1f/07/1f0736f6dca98a77.png",
        name: "Lama3L9R",
        uuid: "abcde-fghij-klmno-pqrst",
      }}
    />,
    document.getElementById("root")
  );
}

const register = () => {
  alert("Sign Up");
}

const main = () => (
  <div className="signin container">
    <h1>{context.signIn}</h1>
    <label className="motd">{config.motd}</label>
    <div className="signin login-container">
      <div className="signin input-container">
        <input label={context.userName} id="username" type="text" placeholder={context.userName}></input>
      </div>

      <div className="signin input-container">
        <input label={context.password} id="password" type="password" placeholder={context.password}></input>
      </div>
    </div>
    <button className="signin btn" onClick={
      click
    }
    >{context.signIn}</button>
    <p>
      {context.notHaveAccount}? <a className="signin register-link" onClick={register}>{context.register}</a>

    </p>

  </div>
)

export default main;