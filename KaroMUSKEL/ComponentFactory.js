var createADiv = function(id, content, aClass, divClass, onclick) {
	var a = document.createElement("a");
	a.id = id;
	if(aClass) a.classList.add(aClass);
	var div = document.createElement("div");
	div.innerHTML = content;
	if(divClass) div.classList.add(divClass);
	a.appendChild(div);
	a.onclick = onclick;
	return a;
};

var createCloseButton = function() {	
	return createADiv(null, "x", "close_button", "frame", function() { tabs.select(tabIndex); });
};