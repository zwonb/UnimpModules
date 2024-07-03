let util = {}
/*
* 获取当前页面地址
* isParam=true时返回完整路径+参数，false是只返回url
*/
function getPageUrl(isParam) {

  let pages = getCurrentPages()    //获取加载的页面
  let currentPage = pages[pages.length-1]    //获取当前页面的对象
  let url = currentPage.route    //当前页面url

  if(isParam){

    let options = currentPage.options    //如果要获取url中所带的参数可以查看options
  
    let param='';
    for (let key in options) {
      const value = options[key]
      param+=key+'='+value+'&'
    }
  
    let urlParam = param!='' ? '?'+param.substring(0,param.length - 1) : '';
    
    url = url+urlParam;

  }

  return url;
}

/*
* 获取日期
* day=0,获取当天，-1昨天+1明天，以此类推
*/
function getDay (day){

  var today = new Date();
  var targetday_milliseconds=today.getTime() + 1000*60*60*24*day;
  today.setTime(targetday_milliseconds); //注意，这行是关键代码
  var tYear = today.getFullYear();
  var tMonth = today.getMonth();
  var tDate = today.getDate();
  tMonth = doHandleMonth(tMonth + 1);
  tDate = doHandleMonth(tDate);
  return tYear+"-"+tMonth+"-"+tDate;

}

//个位数日期补0
function doHandleMonth(month){
  var m = month;
  if(month.toString().length == 1){
    m = "0" + month;
  }
  return m;
}

util.getPageUrl = function (isParam) { 
	return getPageUrl(isParam); 
}

util.getDay = function (day) { 
	return getDay(day); 
}

export default util
