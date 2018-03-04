document.onkeydown = function (e) {
    var ev = window.event || e;
    var code = ev.keyCode || ev.which;
    if (code == 116) { // F5
    	top.$(".menuSelected").click();
        ev.keyCode ? ev.keyCode = 0 : ev.which = 0;
        cancelBubble = true;
        return false;
    }
}


$(function(){
	
	// 增加排序图标
	$("th[data-order-name]").each(function(){
		if (!$(this).hasClass("page-sorting-asc") && !$(this).hasClass("page-sorting-desc")) {
			$(this).addClass('page-sorting');
		}
		$(this).append('<span class="glyphicon"></span>');		
	});
	
	var loading = '<div class="modal fade" id="loading" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" data-backdrop="static">\
		<div class="modal-dialog" role="document"><div class="modal-content"><div style="padding-top:13px;">\
		<span class="glyphicon glyphicon-info-sign"></span>请稍候...</div></div></div></div>';
	$('body').append(loading);
	
	$('body').append('<div id="myAlert"><strong id="myAlertMsg"></strong></div>');
	
	
	var confirm = '<div class="modal fade" id="myConfig"><div class="modal-dialog"><div class="modal-content"><div class="modal-header">\
		<button aria-hidden="true" class="close" data-dismiss="modal" type="button">×</button><h5 class="modal-title">确认</h5></div>\
		<div class="modal-body"><div class="form-group" style="text-align: center;margin-bottom:-5px"><span id="myConfigMsg"></span></div></div>\
		<div class="modal-footer"><button class="btn btn-primary btn-sm" onclick="myConfigOk()" type="button">确定</button>\
		<button class="btn btn-default btn-sm" data-dismiss="modal" type="button">关闭</button></div></div></div></div>';
	$('body').append(confirm);
	
});

function showLoading() {
	$('#loading').modal('show');
	$($(".modal-backdrop")[$(".modal-backdrop").length - 1]).css("z-index", "9999");
	
	$("#loading .modal-content").hide();
	$($(".modal-backdrop")[$(".modal-backdrop").length - 1]).css("opacity", "0");
	// n毫秒没有加载完页面才出现loading
	$("#loading").data("isShowLoading", true);
	setTimeout(function() {
		if ($("#loading").data("isShowLoading")) {
			$("#loading .modal-content").show();
			$(".modal-backdrop").css("opacity", "0.5");
		}
	}, 300);
}

function hideLoading() {
	$("#loading").data("isShowLoading", false);
	$('#loading').modal('hide');
	
}

$.ajaxSetup({
	error: function(r, textStatus, errorThrown) {
		$('#loading').modal('hide');
		alertDanger("error:" + r.status + "|" + this.url);
	}
});

function alertSuccess(msg) {msg=msg==undefined?"操作成功！":msg;alertShow(msg, "alert-success", 1200)}
function alertInfo(msg) {alertShow(msg, "alert-info", 1200)}
function alertWarning(msg) {alertShow(msg, "alert-warning", 1200)}
function alertDanger(msg) {alertShow(msg, "alert-danger", 2000)}

function confirm(msg, func) {
	$("#myConfig").data("func", func);
	$("#myConfigMsg").text(isNaN(msg)?msg:"确定要删除"+msg+"？");
	$("#myConfig").modal('show');
}

function myConfigOk() {
	$("#myConfig").data("func").call();
	$("#myConfig").modal('hide');
}

function alertShow(msg, cls, time) {
	$("#myAlertMsg").text(msg);
	$("#myAlert").attr("class", "alert " + cls);
	$("#myAlert").show();
	setTimeout('$("#myAlert").hide();', time);
}

// 分页begin >>>>>>
function activeSorting() {
	$(".page-sorting, .page-sorting-asc, .page-sorting-desc").unbind("click");
	$(".page-sorting, .page-sorting-asc, .page-sorting-desc").click(function(){
		var orderName = $(this).attr("data-order-name");		
		if ($(this).hasClass("page-sorting-desc")) {
			$("#orderName").val(orderName);
			$("#orderType").val("asc");
			
			$(".page-sorting-asc, .page-sorting-desc").addClass("page-sorting");
			$(".page-sorting-asc").removeClass("page-sorting-asc");
			$(".page-sorting-desc").removeClass("page-sorting-desc");
			$(this).addClass("page-sorting-asc");
		} else {
			$("#orderName").val(orderName);
			$("#orderType").val("desc");
			
			
			$(".page-sorting-asc, .page-sorting-desc").addClass("page-sorting");
			$(".page-sorting-asc").removeClass("page-sorting-asc");
			$(".page-sorting-desc").removeClass("page-sorting-desc");
			$(this).addClass("page-sorting-desc");
		}
		queryPage(1);
	});
}

function refreshPageData(data, pageTemplateId) {
	if (!data) return;
	if (!pageTemplateId) pageTemplateId = "pageTemplate";
	
	$("#" + pageTemplateId).parent().find(">tr:gt(0)").remove();
	$("#" + pageTemplateId).parent().append(template(pageTemplateId, data));
	if (data.page) refreshFooter(data.page);
	if (data.springDataPageable) refreshFooter(data.springDataPageable);
}

function refreshFooter(p) {
	$("#pageNumUL").empty();
	// p.pageSize每页几个记录 p.totalRows总记录数 p.pageNumber当前页 
	$("#totalRows").text(p.totalRows);
	if (p.totalRows == 0) return;
	
	activeSorting();
	
	var pageTotalNum = Math.ceil(p.totalRows/p.pageSize);
	if (p.pageNumber == 1) {
		$("#pageNumUL").append('<li class="disabled"><a>«</a></li>');
	}
	else {
		$("#pageNumUL").append('<li><a href="#this" onclick="queryPage(1)">«</a></li>');
	}
	
	var begin = p.pageNumber <= 3 ? 1 : p.pageNumber - 2;
	for (var i = begin; i < begin + 5 && i <= pageTotalNum; i++) {
		var activeClass = "";
		if (i == p.pageNumber) activeClass = 'class="active"';
		$("#pageNumUL").append('<li ' + activeClass + '><a href="#this" onclick="queryPage(' + i + ')">' + i + '</a></li>');
	}
	
	if (p.totalRows == 0 || p.pageNumber == pageTotalNum) {
		$("#pageNumUL").append('<li class="disabled"><a href="#this">»</a></li>')
	}
	else {
		$("#pageNumUL").append('<li><a href="#this" onclick="queryPage(' + pageTotalNum + ')">»</a></li>');
	}
}
//分页end >>>>>>






























/* honeySwitch */
var honeySwitch = {};
honeySwitch.themeColor = "rgb(100, 189, 99)";
honeySwitch.init = function() {
	var s = "<span class='slider'></span>";
	$("[class^=switch]").append(s);
	$("[class^=switch]").click(function() {
		if ($(this).hasClass("switch-disabled")) {
			return;
		}
		if ($(this).hasClass("switch-on")) {
			$(this).removeClass("switch-on").addClass("switch-off");
			$(".switch-off").css({
				'border-color' : '#dfdfdf',
				'box-shadow' : 'rgb(223, 223, 223) 0px 0px 0px 0px inset',
				'background-color' : 'rgb(255, 255, 255)'
			});
		} else {
			$(this).removeClass("switch-off").addClass("switch-on");
			if (honeySwitch.themeColor) {
				var c = honeySwitch.themeColor;
				$(this).css({
					'border-color' : c,
					'box-shadow' : c + ' 0px 0px 0px 16px inset',
					'background-color' : c
				});
			}
			if ($(this).attr('themeColor')) {
				var c2 = $(this).attr('themeColor');
				$(this).css({
					'border-color' : c2,
					'box-shadow' : c2 + ' 0px 0px 0px 16px inset',
					'background-color' : c2
				});
			}
		}
	});
	window.switchEvent = function(ele, on, off) {
		$(ele).click(function() {
			if ($(this).hasClass("switch-disabled")) {
				return;
			}
			if ($(this).hasClass('switch-on')) {
				if ( typeof on == 'function') {
					on(this);
				}
			} else {
				if ( typeof off == 'function') {
					off(this);
				}
			}
		});
	}
	if (this.themeColor) {
		var c = this.themeColor;
		$(".switch-on").css({
			'border-color' : c,
			'box-shadow' : c + ' 0px 0px 0px 16px inset',
			'background-color' : c
		});
		$(".switch-off").css({
			'border-color' : '#dfdfdf',
			'box-shadow' : 'rgb(223, 223, 223) 0px 0px 0px 0px inset',
			'background-color' : 'rgb(255, 255, 255)'
		});
	}
	if ($('[themeColor]').length > 0) {
		$('[themeColor]').each(function() {
			var c = $(this).attr('themeColor') || honeySwitch.themeColor;
			if ($(this).hasClass("switch-on")) {
				$(this).css({
					'border-color' : c,
					'box-shadow' : c + ' 0px 0px 0px 16px inset',
					'background-color' : c
				});
			} else {
				$(".switch-off").css({
					'border-color' : '#dfdfdf',
					'box-shadow' : 'rgb(223, 223, 223) 0px 0px 0px 0px inset',
					'background-color' : 'rgb(255, 255, 255)'
				});
			}
		});
	}
};
honeySwitch.showOn = function(ele) {
	$(ele).removeClass("switch-off").addClass("switch-on");
	if(honeySwitch.themeColor){
		var c = honeySwitch.themeColor;
		$(ele).css({
			'border-color' : c,
			'box-shadow' : c + ' 0px 0px 0px 16px inset',
			'background-color' : c
		});
	}
	if ($(ele).attr('themeColor')) {
		var c2 = $(ele).attr('themeColor');
		$(ele).css({
			'border-color' : c2,
			'box-shadow' : c2 + ' 0px 0px 0px 16px inset',
			'background-color' : c2
		});
	}
}
honeySwitch.showOff = function(ele) {
	$(ele).removeClass("switch-on").addClass("switch-off");
	$(".switch-off").css({
		'border-color' : '#dfdfdf',
		'box-shadow' : 'rgb(223, 223, 223) 0px 0px 0px 0px inset',
		'background-color' : 'rgb(255, 255, 255)'
	});
}
$(function() {
	honeySwitch.init();
}); 





