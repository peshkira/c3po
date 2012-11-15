$(document).ready(function(){

	//###############################
	//#			Navigation			#
	//###############################

	// Requried: Navigation bar drop-down
	$("nav ul li").hover(function() {
		$(this).addClass("active");
		$(this).find("ul").show().animate({opacity: 1}, 400);
	},function() {
		$(this).find("ul").hide().animate({opacity: 0}, 200);
		$(this).removeClass("active");
	});

	// Requried: Addtional styling elements
	$('nav ul li ul li:first-child').prepend('<li class="arrow"></li>');
	$('nav ul li:first-child').addClass('first');
	$('nav ul li:last-child').addClass('lastnav');
	$('nav ul li ul').parent().append('<span class="dropdown"></span>').addClass('drop');

	// Add click events on buttons in navigation bar.
	var visible = false;
	$('#collections_btn').click(function(event) {
		visible = $("#collections").is(":visible");
		if (visible) {
			$('#collections').slideUp("fast");
		} else {
			$('#collections').slideDown("normal");
		}
		changeSetting('client.collections.show', !visible);
	});

	$('#filter_btn').click(function(event) {
		visible = $("#filter").is(":visible");
		if (visible) {
			$('#filter').slideUp("fast");
		} else {
			$('#filter').slideDown("normal");
		}
		changeSetting('client.filter.show', !visible);
	});

	//###############################
	//#		  Collection Bar		#
	//###############################

	// add click event on the collection chooser html select element
	$("#collections select").change(function() {
		var collection = $(this).val();
		$.ajax ({
			type:     'POST',
			url:      '/c3po/collections?name='+collection,
			timeout:  5000,
			success:  function (oData) {
				startSpinner();
				$('#ajax_collection').text(collection);
				setTimeout(function() {
					window.location.reload();
				}, 300);
			}
		});
	});


	//###############################
	//#			  Filter Bar		#
	//###############################

	// build the current filter elements.
	$.get('/c3po/filters', function(data) {
		$.each(data, function(i, pvf) {
			addNewFilter();
			var div = $('.propertyfilter')[i];
			$(div).children('select').val(pvf.property);
			$(div).attr('oldValue', pvf.property);
			showValuesSelect(div, pvf);
			$(div).children('select:last').val(pvf.selected); 
		});

	});


	//###############################
	//#			   Popup		   	#
	//###############################
	$('#filterpopup .close').click(function(){
		hideValueOptionDialog(false);
	})


	//###############################
	//#			  Feedback			#
	//###############################

	// add feedback animation
	$('#feedback').hover(function() {
		$(this).animate({
			right: '+=10',
		}, 200, function() {}
		);
	}, function() {
		$(this).animate({
			right: '-=10',
		}, 200, function() {}
		);
	});

	// add click event on feedback
	//TODO add some helper functions for building the popup and optimize
	$('#feedback').click(function() {
		$("#overlay").addClass('activeoverlay');
		var popup = $('#filterpopup');
		popup.children('.popupreason').html('Do you have feedback or questions?<br/>Let us know and we will get right back to you by email.');
		popup.children('.popupconfig').append('<input id="mail" type="text" placeholder="Your Email" /><br/>');
		popup.children('.popupconfig').append('<textarea id="message" placeholder="Feedback"></textarea>');
		var button = $('<a class="green_button" href="#">Send</a>').appendTo(popup.children('.popupconfig'));
		popup.css({'display': 'block', 'z-index':11});

		button.click(function() {
			var mail = $('#mail').val()
			var msg = $('#message').val();
			var data = {'email': mail, 'message':msg};
			$.ajax ({
				type:     'POST',
				url:      '/c3po/feedback',
				dataType: 'json',
				data: data,
				timeout:  5000,
				async: false,
				success:  function (oData) {
					hideValueOptionDialog(false);                                        
				},
				error:    function (oData) {
					$('.popupreason').fadeOut('fast', function() {
						$(this).css({'color': 'red'}).html('An error occurred while sending the email! Error message is: <br />' + oData.responseText).fadeIn('slow');
					})
				}
			});
		})
	});

}); // end document ready

//global

//###############################
//#			  Spinner			#
//###############################
function getSpinnerOpts() {
	var opts = {
			lines: 13, // The number of lines to draw
			length: 7, // The length of each line
			width: 4, // The line thickness
			radius: 10, // The radius of the inner circle
			rotate: 0, // The rotation offset
			color: '#FFF', // #rgb or #rrggbb
			speed: 1, // Rounds per second
			trail: 80, // Afterglow percentage
			shadow: true, // Whether to render a shadow
			hwaccel: true, // Whether to use hardware acceleration
			className: 'spinner', // The CSS class to assign to the
			// spinner
			zIndex: 2e9, // The z-index (defaults to 2000000000)
			top: 'auto', // Top position relative to parent in px
			left: 'auto' // Left position relative to parent in px
	};
	return opts;
}

//add spin function to jQuery 
$.fn.spin = function(opts) {
	this.each(function() {
		var $this = $(this),
		data = $this.data();

		if (data.spinner) {
			data.spinner.stop();
			delete data.spinner;
		}
		if (opts !== false) {
			data.spinner = new Spinner($.extend({color: $this.css('color')}, opts)).spin(this);
		}
	});
	return this;
};

//start spinner
function startSpinner() {
	$('#spinner').css("opacity", "1");
	$('#spinner').spin(getSpinnerOpts());

};

//stop spinner
function stopSpinner() {
	$('#spinner').spin(false);
	$('#spinner').css("opacity", "0");

};


//###############################
//#			  Filter			#
//###############################
function addNewFilter() {
	$.ajax ({
		headers: { 
			Accept : "application/json; charset=utf-8",
		},
		type:     'GET',
		url:      '/c3po/properties',
		timeout:  5000,
		async: false,
		success:  function (oData) {
			addNewPropertiesSelect(oData); 

		}
	});

};

//adds a new select with all properties for the next filter selection
function addNewPropertiesSelect(properties) {
	// check if the previous filter is already setup accordingly
	var show = false; 
	if ($('.propertyfilter:last')[0]) {
		var selects = $('.propertyfilter:last select');
		if(selects.length > 1 && $(selects[1]).val() !== "") {
			show = true;
		} else {
			$('.propertyfilter:last').effect("shake", {distance:10, times:3, direction: 'up'}, 80);
		}
	} else {
		show = true;
	}

	// if previous filter is ready, then show
	if (show) {
		// create new div
		var div = $('<div class="propertyfilter"></div>').appendTo('#filter');

		// append delete button and install delete handler
		var deletediv = $('<div class="delete"><a class="red_button" href="#">x</a></div>').appendTo($(div));
		$(deletediv).click(function() {
			var property = $(this).siblings('select:first').val();
			$.ajax({
				type:     'DELETE',
				url:      '/c3po/filter?property=' + property,
				timeout:  5000,
				success:  function(oData) {
					window.location.reload();
				}
			});
			$(this).parent().remove();

		});

		// for each property add an option in the html select
		var sel = $('<select>').appendTo($(div));
		$(sel).append($('<option>').text("").attr('value',''));
		$.each(properties, function(i, value) {
			$(sel).append($('<option>').text(value).attr('value', value));
		});

		// if the select is clicked then get the values for the current
		// option.
		$(sel).change(function() {
			var property = $(this).val();
			if (property) {
				var old = $(this).parent().attr('oldValue');
				$(this).parent().attr('oldValue', property);
				$.ajax ({
					type:     'DELETE',
					async:    false,
					url:      '/c3po/filter?property='+old,
					timeout:  5000
				});

				var collection = "";
				$.ajax ({
					type:     'GET',
					async:    false,
					url:      '/c3po/settings?key=current.collection',
					timeout:  5000,
					success: function (oData) {
						collection = oData;
					}
				});

				var url = '/c3po/filter/values?filter='+property+'&collection=' + collection;

				$.ajax ({
					type:     'GET',
					async:    false,
					url:      '/c3po/property?name=' + property,
					timeout:  5000,
					success: function (oData) {
						if (oData.type == 'INTEGER') {
							showIntegerPropertyDialog('getValuesForProperty("' + url + '")');

						} else { 
							showOtherProperty(url, div);
						}	    
					}
				});


			} else {
				$(sel).effect("highlight", {color:'#FF1400'} , "slow");
				if ($(div).children('select').length > 1) {
					($(div).children('select:last')).remove();
				}
			}
		});
	}
};

function showOtherProperty(url, div) {
	$.ajax ({
		type:     'GET',
		url:      url,
		timeout:  5000,
		success:  function (oData) {
			showValuesSelect(div, oData);
		}
	});
}

function showValuesSelect(filterdiv, pvf) {
	// first remove filtervalues if property was already selected
	if ($(filterdiv).children('select').length > 1) {
		($(filterdiv).children('select:last')).remove();
	}

	var type = pvf.type;
	var sel = $('<select>').appendTo($(filterdiv));
	var values = pvf.values;

	$(sel).append($('<option>').text("").attr('value',''));
	$.each(values, function(i, value) {
		$(sel).append($('<option>').text(value).attr('value', value));
	});

	$(sel).change(function() {
		var value = $(this).val();

		if (value) {
			startSpinner();
			$.post('/c3po/filter?filter=' + pvf.property + '&value='+value + '&type=normal', function(data) {
				window.location.reload();
			});
		} else {
			$(sel).effect("highlight", {color:'#FF1400'} , "slow");
		}
	});
};

function showIntegerPropertyDialog(func) {
	$("#overlay").addClass('activeoverlay');
	var popup = $('#filterpopup');
	popup.children('.popupreason').text('This is an integer property and it requires the selection of an algorithm for the histogram bins.');
	popup.children('.popupconfig').append($('<select><option/><option value="fixed">fixed</option><option value="sturge">Sturge\'s</option><option value="sqrt">Square-root choice</option></select>'));
	popup.css({'display': 'block', 'z-index':11});

	$('.popupconfig select:last').change(function() {

		$('.popupconfig .green_button').remove();
		$('.popupconfig input').remove();
		var val = $(this).val();
		if (val == "") {
			$(this).effect("highlight", {color:'#FF1400'} , "slow");
		}else { 
			if (val == "fixed") {
				$('.popupconfig').append($('<input type="text" placeholder="bin width" />'));
			}

			$('.popupconfig').append($('<a class="green_button" href=javascript:'+ func + '>Apply</a>'))
		}
	});
}

function getValuesForProperty(url) {
	url = getUrlForIntegerProperty(url);

	$.ajax ({
		type:     'GET',
		url:      url,
		timeout:  5000,
		success:  function (oData) {
			var div = $(".propertyfilter:last");
			hidePopupDialog();
			showValuesSelect(div, oData);
		}
	});
}

function getUrlForIntegerProperty(url) {
	var selects = $('.popupconfig').children('select');
	var property = $('.popupconfig').children('select:first').val();
	var alg = $('.popupconfig').children('select:last').val();
	var width = -1;
	url += "&alg=" + alg

	if (alg == "fixed") {
		width = $('.popupconfig input:first').val();
		url += "&width=" + width;
	}

	return url;
}

function hidePopupDialog() {
	$('#overlay').removeClass('activeoverlay');
	var popup = $('#filterpopup');
	popup.css({'display':'none'});
	popup.children('.popupconfig').children().remove(); 
} 

function changeSetting(setting, value) {
	$.ajax ({
		type:     'POST',
		url:      '/c3po/settings?setting='+setting+'&value='+value,
		timeout:  5000,
		async:    false
	});
}; 

function showValueOptionDialog() {
	$("#overlay").addClass('activeoverlay');

	var popup = $('#filterpopup');
	popup.children('.popupreason').text('This is an integer property and it requires the selection of an algorithm for the histogram bins.');
	popup.children('.popupconfig').append($('<select><option value="fixed">fixed</option><option value="sturge">Sturge\'s</option><option value="sqrt">Square-root choice</option></select>'));
	popup.css({'display': 'block', 'z-index':11});

	$('.popupconfig select').click(function() {
		$('.popupconfig .green_button').remove();
		$('.popupconfig input').remove();
		if ($(this).val() == "fixed") {
			$('.popupconfig').append($('<input type="text" placeholder="nr of bins" />'));
		}

		$('.popupconfig').append($('<a class="green_button" href="javascript:hideValueOptionDialog(true)">Apply</a>'))
	});
};

function hideValueOptionDialog(apply) {
	$('#overlay').removeClass('activeoverlay');
	var popup = $('#filterpopup');
	popup.css({'display':'none'});
	$('.popupreason').css({'color': '#888'});
	popup.children('.popupconfig').children().remove(); 
}         
