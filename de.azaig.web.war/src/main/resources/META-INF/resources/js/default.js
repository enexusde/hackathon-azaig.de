json = {
	headers : {
		'Content-Type' : 'application/json',
	}
};
function post(json) {
	return {
		method : 'POST',
		headers : {
			'Content-Type' : 'application/json',
		},
		body : JSON.stringify(json)
	};
};
dge = function(id) {
	return document.getElementById(id);
};
fetch('api/info',post({}))
	.then(a=>a.json())
	.then(j=>{
		document.body.setAttribute("loggedin",typeof j.messages !== 'undefined');
		asjst.render('info',j,function(html){
			dge('infoline').innerHTML=html;
		});
	});

function search(term){
	
}