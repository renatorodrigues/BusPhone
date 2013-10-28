var util = require("./util");
var passwordHash = require('password-hash');
require('date-utils');

/*module.exports = { auth_user_by_token:function(a){}};*/


/* Client login*/

 exports.login = function(req, res){
	var db = req.db;
	
	var username = req.body.username,
		password = req.body.password,
		token = req.body.token;

	if(!( (username!=null && password!=null) || token!=null)){
		return util.out(res,2);
	}

	if(token!=null){
		auth_user_by_token(db,token, function(id){

			if(id==null){
				if(username!=null && password!=null)
					auth_user_by_creds(db, username,password);
				else{
					return util.out(res,5);
				}

			} else {
				return_user_token(db,res,id,true);
			}
		});
	}
	else {
		auth_user_by_creds(db,res,username,password);
	}		
};

var auth_user_by_token = function(db,token,next){
	db.serialize(function() {
		db.get('SELECT clientID FROM clients WHERE token=$token',{ $token:token },
			function(err,row){

				if(err!=null){return util.out(res,3,{err:err.toString()});}

				if (typeof(row) != 'undefined')
					next(row.clientID);
				else
					next(null);
			}
		);
	});
}

exports.auth_user = function(res,db,token,next){
	return auth_user_by_token(db,token,function(id){
		if(id==null)
			util.out(res,5);
		else
			next(id);
	});
};

function auth_user_by_creds(db,res,username,password){
	db.serialize(function() {
		db.get('SELECT clientID,password,token FROM clients WHERE username=$username',{ $username:username },
			function(err,row){

				if(err!=null)  return util.out(res,3,{err:err.toString()}); 

				if(typeof(row) != 'undefined' && passwordHash.verify(password,row.password)){
				
					return_user_token(db,res,row.clientID);
					
				} else {
					 return util.out(res,1);
				}
			}
		);
	});
}

function return_user_token(db,res,id, bytoken){
	bytoken = bytoken || false;
	db.serialize(function() {
		db.get("SELECT token,lastLogged FROM clients WHERE clientID=$id AND lastLogged IS NOT NULL AND lastLogged >= Datetime('now','localtime','-10 day')",{ $id:id },
			function(err,row){

				if(err!=null){
					return util.out(res,3,{err:err.toString()});
				}

				if (typeof(row) == 'undefined')
				{
					if(bytoken){
						return util.out(res,6);
					}
					else{
						gen_user_undup_token(db,res,id);
					}
				}
				else{
					return util.out(res,0,{token:row.token});
				}	
			}
		);
	});
}

function gen_user_undup_token(db,res,id){
	var token = util.gen_uuid();
	db.serialize(function() {
		db.get('SELECT token FROM clients WHERE token=$token',{ $token:token },
			function(err,rows){ 
				if (err==null && typeof(rows) == 'undefined'){			
					add_token_to_user(db,res,id,token);
				}
				else{ 
					gen_user_undup_token(db,res,id);
				}
			}
		);
	});
}

function add_token_to_user(db,res,id,token){
	db.serialize(function() {
		var q = db.prepare("UPDATE clients SET token=$token,lastLogged=Datetime('now','localtime') WHERE clientID=$id");
		q.run({
			$id: id,
			$token: token
		},function(err,row){
			console.log("e: "+err);
			if(err==null){
				return util.out(res,0,{token:token});
			}
			else{
				return util.out(res,3,{err:err.toString()});
			}
		});
		
	});
}

/* Client register */

exports.register = function(req, res){

	console.log(req.body.name);

	var db = req.db;
	var username = req.body.username,
		name = req.body.name,
		password = req.body.password,
		credit_card = req.body.creditcard;

	if( username==null || name==null || 
		password==null || credit_card==null){
		return util.out(res,2);
	}

	password = passwordHash.generate(password);

	db.serialize(function() {
		db.get('SELECT username FROM clients WHERE username=$username',
			{ $username:username },
			function(err,row){
				if(err==null && typeof(row) == 'undefined' )
					new_client(db,res,username,name,password,credit_card);
				 else {
					 return util.out(res,4);
				 }
			}
		);
	});
};

function new_client(db,res,username,name,password,credit_card){
	var q = db.prepare("INSERT INTO clients(username,name,password,creditCard) VALUES ($username,$name,$password,$credit_card)");
	q.run({
		$username: username,
		$name: name,
		$password: password,
		$credit_card: credit_card
	}, function(){
		return util.out(res,0);
	});
}

/* bus login */

exports.bus_login = function(req, res){
   var db = req.db;
	
	var identifier = req.body.identifier,
		password = req.body.password,
		token = req.body.token;

	if(!( (identifier!=null && password!=null) || token!=null)){
		return util.out(res,2);
	}

	if(token!=null){
		auth_bus_by_token(db,token, function(id){

			if(id==null){
				if(identifier!=null && password!=null)
					auth_bus_by_creds(db, identifier,password);
				else{
					return util.out(res,5);
				}

			} else {
				return_bus_token(db,res,id,true);
			}
		});
	}
	else {
		auth_bus_by_creds(db,res,identifier,password);
	}		
};

function auth_bus_by_creds(db,res,identifier,password){
	db.serialize(function() {
		db.get('SELECT busID,password,token FROM bus WHERE identifier=$identifier',{ $identifier:identifier },
			function(err,row){

				if(err==null && typeof(row) != 'undefined' && passwordHash.verify(password,row.password)){
					
					return_bus_token(db,res,row.busID);
					
				} else {
					return  util.out(res,1);
				}
			}
		);
	});
}

var auth_bus_by_token = function(db,token,next){
	db.serialize(function() {
		db.get('SELECT busID FROM bus WHERE token=$token',{ $token:token },
			function(err,row){

				if(err!=null){
					return util.out(res,3,{err:err.toString()});
				}

				if (typeof(row) != 'undefined')
					next(row.busID);
				else
					next(null);
			}
		);
	});
}

exports.auth_bus = function(res,db,token,next){
	return auth_bus_by_token(db,token,function(id){
		if(id==null)
			util.out(res,5);
		else
			next(id);
	});
};

function return_bus_token(db,res,id, bytoken){
	bytoken = bytoken || false;
	db.serialize(function() {
		db.get("SELECT token,lastLogged FROM bus WHERE busID=$id AND lastLogged >= Datetime('now','localtime','-10 day')",{ $id:id },
			function(err,row){

				if(err!=null){
					return util.out(res,3,{err:err.toString()});
				}
	
				if (typeof(row) == 'undefined')
				{
					if(bytoken){
						return util.out(res,6);
					}
					else{
						gen_bus_undup_token(db,res,id);
					}
				}
				else{
					return util.out(res,0,{token:row.token});
				}	
			}
		);
	});
}

function gen_bus_undup_token(db,res,id){
	var token = util.gen_uuid();
	db.serialize(function() {
		db.get('SELECT token FROM bus WHERE token=$token',{ $token:token },
			function(err,rows){ 
				if (err==null && typeof(rows) == 'undefined'){			
					add_token_to_bus(db,res,id,token);
				}
				else{ 
					gen_bus_undup_token(db,res,id);
				}
			}
		);
	});
}

function add_token_to_bus(db,res,id,token){
	db.serialize(function() {
		var q = db.prepare("UPDATE bus SET token=$token,lastLogged=Datetime('now','localtime') WHERE busID=$id");
		q.run({
			$id: id,
			$token: token
		},function(err,row){

			if(err==null){
				return util.out(res,0,{token:token});
			}
			else{
				return util.out(res,3,{err:err.toString()});
			}
		});
		
	});
}

/* bus register */

exports.bus_register = function(req, res){

	var db = req.db;
	var identifier = req.body.identifier,
		password = req.body.password;

	if( identifier==null || password==null){
		return util.out(res,2);
	}

	password = passwordHash.generate(password);

	db.serialize(function() {
		db.get('SELECT identifier FROM bus WHERE identifier=$identifier',
			{ $identifier:identifier },
			function(err,row){
				if(err==null && typeof(row) == 'undefined' )
					new_bus(db,res,identifier,password);
				 else {
					return util.out(res,4,{token:row.token});
				 }
			}
		);
	});
};

function new_bus(db,res,identifier,password){
	var q = db.prepare("INSERT INTO bus(identifier,password) VALUES ($identifier,$password)");
	q.run({
		$identifier: identifier,
		$password: password
	}, function(){
		return util.out(res,0);
	});
}



