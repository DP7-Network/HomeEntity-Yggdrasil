import React from "react";
import ReactDOM from "react-dom";
import signIn from "./page/signin";
import "./fonts.css";
import "./index.css";

function Main() {
  return signIn();
}

ReactDOM.render(<Main />, document.getElementById("root"));
