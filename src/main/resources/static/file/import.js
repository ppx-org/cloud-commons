var imp = {};
imp.areaScroll = function(obj) {
	$("#rownum").prop("scrollTop", obj.scrollTop);
}
imp.rownumScroll = function(obj) {
	$("#content").prop("scrollTop", obj.scrollTop);
}
imp.areakeyup = function(obj) {
	if (event.keyCode != 13) return;
	$("#areaObj").data("currentObj", obj);
	this.createRownum();
}
imp.createRownum = function (){
	var currentObj = $("#areaObj").data("currentObj");
	
	var lineNum = $(currentObj).val().split("\n").length;
	var lineNumArray = [];
	for (var i = 1; i <= lineNum; i++) {
		lineNumArray.push(i)
	}
	$("#rownum").val(lineNumArray.join("\n"));
}
imp.areaPaste = function(obj) {
	$("#areaObj").data("currentObj", obj);
	
	// 去掉空行
	//var val = $.trim($(obj).val());
	//$(obj).val(val == "" ? "" : val + "\n");
	
	window.setTimeout(function() {
		imp.createRownum();
		imp.areaScroll($("#areaObj").data("currentObj"));
	}, 100);
}
imp.gotoRownum = function(n) {
	var lineNum = $("#rownum").val().split("\n").length;
	
	if (n < '0' || n > lineNum) {
		return;
	}
	
	var enter = "";
	for (var i = 0; i < new Number(n); i++) {
		enter += (i + 1) + "\n";
	}
	$("#rownum").val(enter).focus();
	
	var enterNew = "";
	for (var i = 0; i < lineNum - new Number(n); i++) {
		enterNew += new Number(n) + (i + 1) + "\n";
	}
	$("#rownum").val($("#rownum").val() + enterNew);
}

