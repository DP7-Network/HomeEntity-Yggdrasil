import * as zh_CN from "./zh-CN";
import * as en_US from "./en-US";
import * as utils from "./utils.js";

const i18n = new utils.Context();

i18n.regiser(zh_CN);
i18n.regiser(en_US);

const Helper = () => {
  if (i18n.find(navigator.language)) return i18n.getContext(navigator.language);
  else return i18n.getContext("en_US");
};
export default Helper;
