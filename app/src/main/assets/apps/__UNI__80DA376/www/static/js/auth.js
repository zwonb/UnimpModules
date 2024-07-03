/*
add zxr
*/

function menuPerm(id){
	// 从本地缓存中异步获取menuArray的内容
	let arr = uni.getStorageSync('sysAuthArr');
	
	let index = arr.indexOf(id);
	let index2 = arr.indexOf(id.toString());
	
	if(index > -1 || index2 > -1){
		return true;
	} else {
		return false;
	}
}

// 查询某个值是否存在于数组中（只支持一维数组）
function arraySearch(value,arr){
	
	if(typeof value != 'string'){
		value = value.toString();
	}
	
	let index = -1;
	
	for(let i in arr){
		
		let v = arr[i];
		
		if(typeof v != 'string'){
			v = v.toString();
		}
		
		if(v == value){
			index = i;
			break;
		}
		
	}
	
	return index;
}

export default {
	menuPerm,
	arraySearch,
}
