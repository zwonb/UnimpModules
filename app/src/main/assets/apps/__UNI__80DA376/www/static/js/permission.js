// null = 未请求，1 = 已允许，0 = 拒绝|受限, 2 = 系统未开启
/**
 * 设备权限判断
 * ===================
 * 返回值：
 * null	 	未请求
 * 1		已允许
 * 0		拒绝|受限
 * 2		系统未开启
 */
var isIOS

function parseAuthStatus(authStatus) { // add by lwz 转换状态值

	let res = 0;

	if(authStatus === 0) {
		res = null;
	}
	else if(authStatus == 3) {
		res = 1;
	}
	else {
		res = 0;
	}

	return res;

}

async function album() {
	var result = 0;
	var PHPhotoLibrary = plus.ios.import("PHPhotoLibrary");
	var authStatus = PHPhotoLibrary.authorizationStatus();

	result = parseAuthStatus(authStatus);

	console.log('permission album:', result);

	if(result === null) {
		// 请求权限
		await requestAlbumAuth();
		authStatus = PHPhotoLibrary.authorizationStatus();
		result = parseAuthStatus(authStatus);
		console.log('request permission album:', result);
	}

	plus.ios.deleteObject(PHPhotoLibrary);
	return result;
}

function requestAlbumAuth() {
	return new Promise((resolve, reject) => {

		plus.ios.invoke(
			'PHPhotoLibrary',
			'requestAuthorization:',
			(e) => {
				resolve();
			},
		);

	});
}

async function camera() {
	var result = 0;
	var AVCaptureDevice = plus.ios.import("AVCaptureDevice");
	var authStatus = AVCaptureDevice.authorizationStatusForMediaType('vide');

	result = parseAuthStatus(authStatus);

	console.log('permission camera:', result);

	if(result === null) {
		// 请求权限
		await requestCameraAuth();
		authStatus = AVCaptureDevice.authorizationStatusForMediaType('vide');
		result = parseAuthStatus(authStatus);
		console.log('request permission camera:', result);
	}

	plus.ios.deleteObject(AVCaptureDevice);
	return result;
}

function requestCameraAuth() {
	return new Promise((resolve, reject) => {

		plus.ios.invoke(
			'AVCaptureDevice',
			'requestAccessForMediaType:completionHandler:',
			'vide',
			(e) => {
				resolve();
			},
		);

	});
}

function location() {
	var result = 0;
	var cllocationManger = plus.ios.import("CLLocationManager");
	var enable = cllocationManger.locationServicesEnabled();
	var status = cllocationManger.authorizationStatus();

	if(!enable) {
		result = 2;
	}
	else if(status === 0) { // 未授权
		result = null;
	}
	/* else if(status === 4){ // 允许一次
		result = null;
	} */
	else if(status === 4 || status === 3) {
		result = 1;
	}
	else {
		result = 0;
	}

	/* if(result === null){ // 请求权限
		result = await requestLocationAuth();
		console.log('request permission location:',result);
	} */

	plus.ios.deleteObject(cllocationManger);
	return result;
}

function requestLocationAuth() {
	return new Promise((resolve, reject) => {

		plus.geolocation.getCurrentPosition(function(p) {

			//console.log('Geolocation\nLatitude:' + p.coords.latitude + '\nLongitude:' + p.coords.longitude + '\nAltitude:' + p.coords.altitude);

			resolve(1);

		}, function(e) {

			resolve(0);

			//console.log('Geolocation error: ' + e.message);

		});

	});
}

function push() {
	var result = 0;
	var UIApplication = plus.ios.import("UIApplication");
	var app = UIApplication.sharedApplication();
	var enabledTypes = 0;
	if(app.currentUserNotificationSettings) {
		var settings = app.currentUserNotificationSettings();
		enabledTypes = settings.plusGetAttribute("types");
		if(enabledTypes == 0) {
			result = 0;
			console.log("推送权限没有开启");
		}
		else {
			result = 1;
			console.log("已经开启推送功能!")
		}
		plus.ios.deleteObject(settings);
	}
	else {
		enabledTypes = app.enabledRemoteNotificationTypes();
		if(enabledTypes == 0) {
			result = 3;
			console.log("推送权限没有开启!");
		}
		else {
			result = 4;
			console.log("已经开启推送功能!")
		}
	}
	plus.ios.deleteObject(app);
	plus.ios.deleteObject(UIApplication);
	return result;
}

async function contact() {
	let result = 0;
	let CNContactStore = plus.ios.import("CNContactStore");
	let cnAuthStatus = CNContactStore.authorizationStatusForEntityType(0);

	result = parseAuthStatus(cnAuthStatus);

	console.log('permission contact:', result);

	if(result === null) {
		// 请求权限
		await requestContactAuth();
		cnAuthStatus = CNContactStore.authorizationStatusForEntityType(0);
		result = parseAuthStatus(cnAuthStatus);
		console.log('request permission contact:', result);
	}

	plus.ios.deleteObject(CNContactStore);
	return result;

	/* if (cnAuthStatus === 0) {
	    result = null;
	} else if (cnAuthStatus == 3) {
	    result = 1;
	} else {
	    result = 0;
	}
	plus.ios.deleteObject(CNContactStore);
	return result; */
}

function requestContactAuth() {
	return new Promise((resolve, reject) => {

		let CNContactStore = plus.ios.newObject("CNContactStore");

		plus.ios.invoke(
			CNContactStore,
			'requestAccessForEntityType:completionHandler:',
			0,
			(e) => {
				resolve();
			},
		);

	});
}

async function record() {
	var result = null;
	var avaudiosession = plus.ios.import("AVAudioSession");
	var avaudio = avaudiosession.sharedInstance();
	var status = avaudio.recordPermission();

	let parseRecordStatus = function(status) {

		let result = null;

		if(status === 1970168948) {
			result = null;
		}
		else if(status === 1735552628) {
			result = 1;
		}
		else {
			result = 0;
		}

		return result;
	}

	result = parseRecordStatus(status);

	console.log('permission record:', result);

	if(result === null) {
		// 请求权限
		await requestRecordAuth();
		status = avaudio.recordPermission();
		result = parseRecordStatus(status);
		console.log('request permission record:', result);
	}

	plus.ios.deleteObject(avaudiosession);
	return result;
}

function requestRecordAuth() {
	return new Promise((resolve, reject) => {

		let AVAudioSession = plus.ios.newObject("AVAudioSession");

		plus.ios.invoke(
			AVAudioSession,
			'requestRecordPermission:',
			(e) => {
				resolve();
			},
		);

	});
}

function calendar() {
	var result = null;
	var EKEventStore = plus.ios.import("EKEventStore");
	var ekAuthStatus = EKEventStore.authorizationStatusForEntityType(0);
	if(ekAuthStatus == 3) {
		result = 1;
		console.log("日历权限已经开启");
	}
	else {
		console.log("日历权限没有开启");
	}
	plus.ios.deleteObject(EKEventStore);
	return result;
}

function memo() {
	var result = null;
	var EKEventStore = plus.ios.import("EKEventStore");
	var ekAuthStatus = EKEventStore.authorizationStatusForEntityType(1);
	if(ekAuthStatus == 3) {
		result = 1;
		console.log("备忘录权限已经开启");
	}
	else {
		console.log("备忘录权限没有开启");
	}
	plus.ios.deleteObject(EKEventStore);
	return result;
}


function requestIOS(permissionID) {
	return new Promise((resolve, reject) => {
		switch(permissionID) {
			case "push":
				resolve(push());
				break;
			case "location":
				resolve(location());
				break;
			case "record":
				resolve(record());
				break;
			case "camera":
				resolve(camera());
				break;
			case "album":
				resolve(album());
				break;
			case "contact":
				resolve(contact());
				break;
			case "calendar":
				resolve(calendar());
				break;
			case "memo":
				resolve(memo());
				break;
			default:
				resolve(0);
				break;
		}
	});
}

function requestAndroid(permissionID) {
	return new Promise((resolve, reject) => {
		plus.android.requestPermissions(
			[permissionID],
			function(resultObj) {
				var result = 0;
				for(var i = 0; i < resultObj.granted.length; i++) {
					var grantedPermission = resultObj.granted[i];
					console.log('已获取的权限：' + grantedPermission);
					result = 1
				}
				for(var i = 0; i < resultObj.deniedPresent.length; i++) {
					var deniedPresentPermission = resultObj.deniedPresent[i];
					console.log('拒绝本次申请的权限：' + deniedPresentPermission);
					result = 0
				}
				for(var i = 0; i < resultObj.deniedAlways.length; i++) {
					var deniedAlwaysPermission = resultObj.deniedAlways[i];
					console.log('永久拒绝申请的权限：' + deniedAlwaysPermission);
					result = -1
				}
				resolve(result);
			},
			function(error) {
				console.log('result error: ' + error.message)
				resolve({
					code: error.code,
					message: error.message
				});
			}
		);
	});
}

function gotoAppPermissionSetting() {
	if(permission.isIOS) {
		var UIApplication = plus.ios.import("UIApplication");
		var application2 = UIApplication.sharedApplication();
		var NSURL2 = plus.ios.import("NSURL");
		var setting2 = NSURL2.URLWithString("app-settings:");
		application2.openURL(setting2);
		plus.ios.deleteObject(setting2);
		plus.ios.deleteObject(NSURL2);
		plus.ios.deleteObject(application2);
	}
	else {
		var Intent = plus.android.importClass("android.content.Intent");
		var Settings = plus.android.importClass("android.provider.Settings");
		var Uri = plus.android.importClass("android.net.Uri");
		var mainActivity = plus.android.runtimeMainActivity();
		var intent = new Intent();
		intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		var uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
		intent.setData(uri);
		mainActivity.startActivity(intent);
	}
}

const permission = {
	get isIOS() {
		return typeof isIOS === 'boolean' ? isIOS : (isIOS = uni.getSystemInfoSync().platform === 'ios')
	},
	requestIOS: requestIOS,
	requestAndroid: requestAndroid,
	gotoAppSetting: gotoAppPermissionSetting
}

export default permission