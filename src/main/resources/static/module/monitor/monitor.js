//设置jQuery Ajax全局的参数 ，统一异常处理
$.ajaxSetup({
	error: function(r, textStatus, errorThrown) {
        switch (r.status) {
            case(500):$('#loading').modal('hide');alertDanger("error 500:" + this.url);break;
            case(404):$('#loading').modal('hide');alertDanger("error 404:" + this.url);break;
            default:$('#loading').modal('hide');alertDanger("未知错误:" + r.status + "|" + this.url);
        }
    }
});

$(function(){
	$('.glyphicon-question-sign').tooltip('hide');
	
	var loading = '<div class="modal fade" id="loading" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" data-backdrop="static">\
		<div class="modal-dialog" role="document">\
			<div class="modal-content"><div style="padding-top:13px;"><span class="glyphicon glyphicon-info-sign"></span>请稍候...</div></div>\
		</div>\
		</div>'
	$('body').append(loading);
	
	$('body').append('<div id="myAlert" style="position:fixed;left:0px;top:0px;width:100%;text-align:center;display:none;z-index:9999"><strong id="myAlertMsg"></strong></div>');
});

function alertSuccess(msg) {alertShow(msg, "alert-success", 1500)}
function alertInfo(msg) {alertShow(msg, "alert-info", 1500)}
function alertWarning(msg) {alertShow(msg, "alert-warning", 1500)}
function alertDanger(msg) {alertShow(msg, "alert-danger", 3000)}
function alertShow(msg, cls, time) {
	$("#myAlertMsg").text(msg);
	$("#myAlert").attr("class", "alert " + cls);
	$("#myAlert").show();	
	setTimeout('$("#myAlert").hide();', time);
}


/* page */
function activeSorting() {
	$(".pageSorting, .pageSortingAsc, .pageSortingDesc").unbind("click");
	$(".pageSorting, .pageSortingAsc, .pageSortingDesc").click(function(){
		var orderName = $(this).attr("orderName");		
		if ($(this).hasClass("pageSortingDesc")) {
			$("#orderName").val(orderName);
			$("#orderType").val("asc");
			$(".pageSorting, .pageSortingAsc, .pageSortingDesc").attr("class", "pageSorting");
			$(this).attr("class", "pageSortingAsc");
		} else {
			$("#orderName").val(orderName);
			$("#orderType").val("desc");
			$(".pageSorting, .pageSortingAsc, .pageSortingDesc").attr("class", "pageSorting");
			$(this).attr("class", "pageSortingDesc");
		}
		queryPage(1);
	});
}

function freshFooter(p) {
	activeSorting();
	// p.pageSize每页几个记录 p.totalRows总记录数 p.pageNum当前页 
	$("#totalRows").text(p.totalRows);
	var pageTotalNum = Math.ceil(p.totalRows/p.pageSize);
	$("#pageNum").empty();
	if (p.pageNum == 1) {
		$("#pageNum").append('<li class="disabled"><a>«</a></li>');
	}
	else {
		$("#pageNum").append('<li><a href="#this" onclick="queryPage(1)">«</a></li>');
	}
	
	var begin = p.pageNum <= 3 ? 1 : p.pageNum - 2;
	for (var i = begin; i < begin + 5 && i <= pageTotalNum; i++) {
		var activeClass = "";
		if (i == p.pageNum) activeClass = 'class="active"';
		$("#pageNum").append('<li ' + activeClass + '><a href="#this" onclick="queryPage(' + i + ')">' + i + '</a></li>');
	}
	
	if (p.totalRows == 0 || p.pageNum == pageTotalNum) {
		$("#pageNum").append('<li class="disabled"><a href="#this">»</a></li>')
	}
	else {
		$("#pageNum").append('<li><a href="#this" onclick="queryPage(' + pageTotalNum + ')">»</a></li>');
	}	
}


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











