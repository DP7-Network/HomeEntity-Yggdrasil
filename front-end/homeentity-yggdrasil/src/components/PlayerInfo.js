import * as skinview3d from "skinview3d";
import i18n from "../i18n/";
import "./PlayerInfo.css";

const context = i18n();

const drawSkin = (canvas, userData) => {
  let skinViewer = new skinview3d.FXAASkinViewer({
    alpha: true,
    canvas: canvas,
    skin: userData.skin,
    cape: userData.cape,
    width: 300,
    height: 400
  });
  let controller = new skinview3d.createOrbitControls(skinViewer);
  controller.enableRotate = true;
  controller.enableZoom = false;
  controller.enablePan = false;
  // eslint-disable-next-line no-unused-vars
  let walk = skinViewer.animations.add(skinview3d.WalkingAnimation);
};

const changePassword = () => {
  alert("changePassword");
};
const changeSkin = () => {
  alert("changeSkin");
};

const PlayerInfo = ({ userData }) => (
  <div className="playerinfo container">
    <div className="playerinfo skin">
      <canvas
        className="playerinfo skinview"
        ref={canavs => drawSkin(canavs, userData)}
      />
    </div>
    <div className="playerinfo info">
      <div className="playerinfo details">
        <ul>
          <li>
            {context.userName}: {userData.name}
          </li>
          <li>
            {context.uuid}: {userData.uuid}
          </li>
        </ul>
      </div>
      <div className="playerinfo action">
        <button onClick={changePassword}>{context.changePassword}</button>

        <button onClick={changeSkin}>{context.changeSkin}</button>
      </div>
    </div>
  </div>
);

export default PlayerInfo;
