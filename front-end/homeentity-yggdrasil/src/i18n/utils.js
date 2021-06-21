export class Context {
  constructor() {
    this.context = {};
    this.maintainer = {};
  }
  regiser({ name, maintainer, context }) {
    this.context[name] = context;
    this.maintainer[name] = maintainer;
  }
  find(name) {
    if (name in this.context) return true;
    else return false;
  }
  getContext(name) {
    if (name in this.context) return this.context[name];
    else return false;
  }
  getMaintainer(name) {
    if (name in this.maintainer) return this.maintainer[name];
    else return false;
  }
}
