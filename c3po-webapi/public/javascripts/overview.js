$(document).ready(
		function() {
			var button = $('<a  href="#" class="green_button">Add Diagram</a>')
					.appendTo($('#more'));
			button.click(function() {
				$.ajax({
					headers : {
						Accept : "application/json; charset=utf-8",
					},
					type : 'GET',
					url : '/c3po/properties',
					timeout : 5000,
					async : false,
					success : function(oData) {
						showPopup(oData);

					}
				});
			});

			var button2 = $('<a  href="#" class="green_button">Reset</a>')
				.appendTo($('#more'));
			button2.click(function() {

				window.location.href = "/c3po/overview/reset";

				
			});

		});

function showPopup(properties) {
	$("#overlay").addClass('activeoverlay');

	var popup = $('#filterpopup');
	popup.children('.popupreason').text('Please select a property');
	var config = popup.children('.popupconfig');

	var sel = $('<select>').appendTo($(config));
	$(sel).append($('<option>').text("").attr('value', ''));
	$.each(properties, function(i, value) {
		$(sel).append($('<option>').text(value).attr('value', value));
	});

	popup.css({
		'display' : 'block',
		'z-index' : 11
	});

	$('.popupconfig select').change(function() {
		$.ajax({
			type : 'GET',
			url : '/c3po/property?name=' + $(this).val(),
			timeout : 5000,
			success : function(oData) {
				showOptions(oData.type);
			}
		});
	});
};

function showOptions(type) {
	if (type == "STRING" || type == "BOOL" || type == "DATE") {
		var property = $('.popupconfig select').val();
		hidePopupDialog();
		startSpinner();
		$.ajax({
			type : 'GET',
			url : '/c3po/overview/graph?property=' + property,
			timeout : 5000,
			success : function(oData) {
				stopSpinner();
				var hist = [];
				$.each(oData.keys, function(i, k) {
					hist.push([ oData.keys[i], parseInt(oData.values[i]) ]);
				});
				var id = oData.property;
				var data = {};
				data[id] = hist;
				drawGraphs(data);
				//scroll to bottom of page.

			}
		});

	} else {
		showIntegerPropertyDialog('applyIntegerHistogramSelection()');
	}
}

function applyIntegerHistogramSelection() {
	var selects = $('.popupconfig').children('select');
	var property = $('.popupconfig').children('select:first').val();
	var alg = $('.popupconfig').children('select:last').val();
	var width = -1;
	if (alg == "fixed") {
		width = $('.popupconfig input:first').val();
	}

	hidePopupDialog();
	startSpinner();
	$.ajax({
		type : 'GET',
		url : '/c3po/overview/graph?property=' + property + "&alg=" + alg
				+ "&width=" + width,
		timeout : 5000,
		success : function(oData) {
			var hist = [];
			$.each(oData.keys, function(i, k) {
				hist.push([ oData.keys[i], parseInt(oData.values[i]) ]);
			});
			var id = oData.property;
			var data = {};
			data[id] = hist;
			$('#' + id).remove(); // remove the old graph if exist
			drawGraphs(data, oData.options);
			stopSpinner();
			//scroll to bottom of page.
		}
	});
};

function getBarChart(ttl) {
	var options = {
		title : ttl,
		seriesDefaults : {
			renderer : $.jqplot.BarRenderer,
			// Show point labels to the right ('e'ast) of each bar.
			// edgeTolerance of -15 allows labels flow outside the grid
			// up to 15 pixels. If they flow out more than that, they
			// will be hidden.
			pointLabels : {
				show : true,
				location : 'n',
				edgeTolerance : -15
			},
			// Rotate the bar shadow as if bar is lit from top right.
			shadowAngle : 70,
			// Here's where we tell the chart it is oriented horizontally.
			rendererOptions : {
				barDirection : 'vertical',
				barWidth : '12'
			},
			color : '#639B00'
		},
		axesDefaults : {
			tickRenderer : $.jqplot.CanvasAxisTickRenderer,
			tickOptions : {
				angle : -30,
				fontSize : '8pt'
			}
		},
		axes : {
			// Use a category axis on the x axis and use our custom ticks.
			xaxis : {
				renderer : $.jqplot.CategoryAxisRenderer,
				tickOptions : {
					formatter : function(format, val) {
						if (val.length > 30) {
							val = val.substring(0, 25) + '...';
						}

						// val = (val.replace(/\.0/g, ""));
						return val;
					}
				}
			},
			// Pad the y axis just a little so bars can get close to, but
			// not touch, the grid boundaries. 1.2 is the default padding.
			yaxis : {
				pad : 1.05,
				tickOptions : {
					formatString : '%d',
				}
			}
		},
		highlighter : {
			show : true,
			tooltipLocation : 'n',
			showTooltip : true,
			useAxesFormatters : true,
			sizeAdjust : 0.5,
			tooltipAxes : 'y',
			bringSeriesToFront : true,
			tooltipOffset : 30,
		},
		cursor : {
			style : 'pointer', // A CSS spec for the cursor type to change the
								// cursor to when over plot.
			show : true,
			showTooltip : false, // show a tooltip showing cursor position.
			useAxesFormatters : true, // wether to use the same formatter and
										// formatStrings
		// as used by the axes, or to use the formatString
		// specified on the cursor with sprintf.
		}

	};

	return options;
};

function getPieChart(ttl) {
	var options = {
		title : ttl,
		seriesDefaults : {
			renderer : $.jqplot.PieRenderer,
			rendererOptions : {
				showDataLabels : true
			}
		},
		legend : {
			show : true,
			location : 'e'
		}
	};

	return options;
};

function prettifyTitle(title) {
	title = title.replace(/_/g, " ");
	return title.replace(/\w\S*/g, function(txt) {
		return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
	});
};

function drawGraphs(data, options) {
	var idx = 0;
	var graphsdiv = $('#graphs');
	$.each(data, function(i, d) {
		var container;
		var clazz;
		if (idx % 2 == 0) {
			container = $('<div class="span-24">').appendTo(graphsdiv);
			clazz = "dia_left";
		} else if (idx % 2 == 1) {
			container = graphsdiv.children('.span-24:last');
			clazz = "dia_right";
		}

		if (d.length > 30) {
			container = $('<div class="span-24">').appendTo(graphsdiv);
			clazz = "dia_full";
			idx++; // if full length skip to next row left
		}

		container.append('<div id="' + i + '" class="' + clazz + '">');
		$('#' + i).bind(
				'jqplotDataClick',
				function(ev, seriesIndex, pointIndex, data) {
					startSpinner();
					var url = '/c3po/filter?filter=' + i + '&value='
							+ pointIndex + '&type=graph';

					if (options) {
						var type = options['type'];
						var alg = options['alg'];
						var width = options['width'];

						if (type == 'INTEGER') {
							url += '&alg=' + alg;

							if (width) {
								url += '&width=' + width;
							}
						}
					}
					$.post(url, function(data) {
						window.location = '/c3po/overview';
					});
				});
		$.jqplot(i, [ d ], getBarChart(prettifyTitle(i)));

		if (idx == 0) {
			idx++; // if first row skip the right and go to next row...
		}
		idx++;
	})
};