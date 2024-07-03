"use weex:vue";

if (typeof Promise !== 'undefined' && !Promise.prototype.finally) {
  Promise.prototype.finally = function(callback) {
    const promise = this.constructor
    return this.then(
      value => promise.resolve(callback()).then(() => value),
      reason => promise.resolve(callback()).then(() => {
        throw reason
      })
    )
  }
};

if (typeof uni !== 'undefined' && uni && uni.requireGlobal) {
  const global = uni.requireGlobal()
  ArrayBuffer = global.ArrayBuffer
  Int8Array = global.Int8Array
  Uint8Array = global.Uint8Array
  Uint8ClampedArray = global.Uint8ClampedArray
  Int16Array = global.Int16Array
  Uint16Array = global.Uint16Array
  Int32Array = global.Int32Array
  Uint32Array = global.Uint32Array
  Float32Array = global.Float32Array
  Float64Array = global.Float64Array
  BigInt64Array = global.BigInt64Array
  BigUint64Array = global.BigUint64Array
};


(()=>{var f=Object.create;var i=Object.defineProperty;var m=Object.getOwnPropertyDescriptor;var v=Object.getOwnPropertyNames;var h=Object.getPrototypeOf,x=Object.prototype.hasOwnProperty;var d=(e,t)=>()=>(t||e((t={exports:{}}).exports,t),t.exports);var y=(e,t,o,r)=>{if(t&&typeof t=="object"||typeof t=="function")for(let n of v(t))!x.call(e,n)&&n!==o&&i(e,n,{get:()=>t[n],enumerable:!(r=m(t,n))||r.enumerable});return e};var _=(e,t,o)=>(o=e!=null?f(h(e)):{},y(t||!e||!e.__esModule?i(o,"default",{value:e,enumerable:!0}):o,e));var p=d((S,w)=>{w.exports=Vue});var U=_(p());function l(e,t,...o){uni.__log__?uni.__log__(e,t,...o):console[e].apply(console,[...o,t])}var g=(e,t)=>{let o=e.__vccOpts||e;for(let[r,n]of t)o[r]=n;return o};var s=_(p()),O={data(){return{webviewStyles:{},url:""}},onLoad(){let e=this;this.getOpenerEventChannel().on("postPageData",function(o){l("log","at pages/web/web.nvue:30",o),e.url=o.url,o.hasOwnProperty("title")&&uni.setNavigationBarTitle({title:o.title})})},methods:{onRefresh(e){l("log","at pages/web/web.nvue:47","uniapp web refresh",e),uni.navigateBack()},goUserHome(e){l("log","at pages/web/web.nvue:53","uniapp web goUserHome",e),uni.reLaunch({url:"/pages/index/index"})},execute(e,t){l("log","at pages/web/web.nvue:61","uniapp web execute\uFF1A",e,t)}}};function C(e,t,o,r,n,a){let b=(0,s.resolveComponent)("webViewComponent");return(0,s.openBlock)(),(0,s.createElementBlock)("scroll-view",{scrollY:!0,showScrollbar:!0,enableBackToTop:!0,bubble:"true",style:{flexDirection:"column"}},[(0,s.createVNode)(b,{style:{flex:"1",width:"750rpx"},loadUrl:n.url,onOnRefresh:a.onRefresh,onOnGoUserHome:a.goUserHome,onOnExecute:a.execute},null,8,["loadUrl","onOnRefresh","onOnGoUserHome","onOnExecute"])])}var u=g(O,[["render",C]]);var c=plus.webview.currentWebview();if(c){let e=parseInt(c.id),t="pages/web/web",o={};try{o=JSON.parse(c.__query__)}catch(n){}u.mpType="page";let r=Vue.createPageApp(u,{$store:getApp({allowDefault:!0}).$store,__pageId:e,__pagePath:t,__pageQuery:o});r.provide("__globalStyles",Vue.useCssStyles([...__uniConfig.styles,...u.styles||[]])),r.mount("#root")}})();
