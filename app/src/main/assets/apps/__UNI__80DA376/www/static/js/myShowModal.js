/**
 * 类uni.showModal()对象
 */

class ShowModal {

	constructor(){
		this.id = 'showModal'; // 弹窗唯一标识
		this.params = {}; // 此次弹窗传入的参数，参数与uni.showModal一致
		
		this.showModal = this.showModal.bind(this);
		this.success = this.success.bind(this);
	}
	
	// 初始化
	init(){
		
		// 注册监听弹窗确认
		uni.$on('showModalSuccess',this.success);
		
	}
	
	// 弹出
	showModal(params){
		let that = this;
		
		that.params = params;
		
		// 通过 id 获取 nvue 子窗体
		const subNVue = uni.getSubNVueById(that.id);
		
		// 打开 nvue 子窗体  
		subNVue.show('fade-in',300,function(){  
		    // 打开后传参
		    uni.$emit('showModalShow',params);
		});  
		
	}
	
	// 弹窗后，用户操作回调
	success(e){

		let callback = this.params.hasOwnProperty('success') ? this.params.success : null;
		
		if(typeof callback == "function"){
			callback(e);
		}
		
	}
}

export default ShowModal