module.exports= {
	gen_uuid: function(){
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
		    var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
		    return v.toString(16);
		});
	},
	out: function(res,code,extra){
		
		var info = "";
		switch(code){
			case 0:
				info="OK";
				break;
			case 1:
				info="bad username/password";
				break;
			case 2:
				info="bad parameters";
				break;
			case 3:
				info="error";
				break;
			case 4:
				info="already in use";
				break;
			case 5:
				info="invalid token";
				break;
			case 6:
				info="token expired";
				break;
			case 7:
				info="excess tickets";
				break;
			case 8:
				info="ticket used";
				break;
		}
		var reply = extend({status:code, info:info},extra);
		res.send(reply);
		return;
	}
};

function extend(target) {
    var sources = [].slice.call(arguments, 1);
    sources.forEach(function (source) {
        for (var prop in source) {
            target[prop] = source[prop];
        }
    });
    return target;
}

