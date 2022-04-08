$(document).ready(function () {

    //###############################
    //#			Navigation			#
    //###############################

    // Requried: Navigation bar drop-down
    $("nav ul li").hover(function () {
        $(this).addClass("active");
        $(this).find("ul").show().animate({opacity: 1}, 400);
    }, function () {
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
    $('#collections_btn').click(function (event) {
        visible = $("#collections").is(":visible");
        if (visible) {
            $('#collections').slideUp("fast");
        } else {
            $('#collections').slideDown("normal");
        }
        changeSetting('client.collections.show', !visible);
    });

    $('#filter_btn').click(function (event) {
        visible = $("#filter").is(":visible");
        if (visible) {
            $('#filter').slideUp("fast");
        } else {
            $('#filter').slideDown("normal");
        }
        changeSetting('client.filter.show', !visible);
    });

    $('#templates_btn').click(function (event) {
        visible = $("#templates").is(":visible");
        if (visible) {
            $('#templates').slideUp("fast");
        } else {
            $('#templates').slideDown("normal");
        }
        changeSetting('client.templates.show', !visible);
    });


    //###############################
    //#		  Collection Bar		#
    //###############################

    // add click event on the collection chooser html select element
    $("#collectionSelect").change(function () {
        var collection = $(this).val();
        $.ajax({
            type: 'POST',
            url: '/c3po/collections?name=' + collection,
            success: function (oData) {
                startSpinner();
                $('#ajax_collection').text(collection);
                setTimeout(function () {
                    window.location.reload();
                }, 300);
            }
        });
    });


    //###############################
    //#			  Filter Bar		#
    //###############################

    $.get('/c3po/filters', function (data) {
        //alert(JSON.stringify(data));
        var div = $('<div class="propertyfilter">').appendTo('#filter');
        var properties = getAvailableProperties();
        for (var i in data) {
            var pfc = data[i];
            renderPFC(pfc, div, properties);
        }
    });


    //we need a function that does the same thing: render the sourced values. The scope of the function is global. All the parameters must be specified on input.


    //###############################
    //#			   Popup		   	#
    //###############################
    $('#filterpopup .close').click(function () {
        hideValueOptionDialog(false);
    });


    //###############################
    //#			  Feedback			#
    //###############################

    // add feedback animation
    $('#feedback').hover(function () {
        $(this).animate({
                right: '+=10'
            }, 200, function () {
            }
        );
    }, function () {
        $(this).animate({
                right: '-=10'
            }, 200, function () {
            }
        );
    });

    // add click event on feedback
    //TODO add some helper functions for building the popup and optimize
    $('#feedback').click(function () {
        $("#overlay").addClass('activeoverlay');
        var popup = $('#filterpopup');
        popup.children('.popupreason').html('Do you have feedback or questions?<br/>Let us know and we will get right back to you by email.');
        popup.children('.popupconfig').append('<input id="mail" type="text" placeholder="Your Email" /><br/>');
        popup.children('.popupconfig').append('<textarea id="message" placeholder="Feedback"></textarea>');
        var button = $('<a class="green_button" href="#">Send</a>').appendTo(popup.children('.popupconfig'));
        popup.css({'display': 'block', 'z-index': 11});

        button.click(function () {
            var mail = $('#mail').val();
            var msg = $('#message').val();
            var data = {'email': mail, 'message': msg};
            $.ajax({
                type: 'POST',
                url: '/c3po/feedback',
                dataType: 'json',
                data: data,
                async: false,
                success: function (oData) {
                    hideValueOptionDialog(false);
                },
                error: function (oData) {
                    $('.popupreason').fadeOut('fast', function () {
                        $(this).css({'color': 'red'}).html('An error occurred while sending the email! Error message is: <br />' + oData.responseText).fadeIn('slow');
                    })
                }
            });
        })
    });

}); // end document ready

//global

function renderPFC(pfc, div, properties) {
    var holderPFC = $('<div class="holderPropertyFilterCondition">').appendTo(div);
    var holderDelete = $('<div class="delete"><a class="red_button" href="#">x</a></div>').appendTo($(holderPFC));
    holderDelete.click(function () {
        $(this).parent().remove();
    });

    //Property name

    holderPFC.append('<text>Property name: </text>');
    var selectPropertyName = $('<select id="selectPropertyName">').appendTo(holderPFC);
    selectPropertyName.append($('<option>').text("").attr('value', ''));
    for (var i in properties) {
        selectPropertyName.append($('<option>').text(properties[i]).attr('value', properties[i]));
    }
    selectPropertyName[0].value = pfc.propertyname;

    //Property status

    holderPFC.append('<text>Property status: </text>');
    var selectPropertyStatus = $('<select id="selectPropertyStatus">').appendTo(holderPFC);
    selectPropertyStatus.append($('<option>').text("").attr('value', ""));
    selectPropertyStatus.append($('<option>').text("OK").attr('value', "OK"));
    selectPropertyStatus.append($('<option>').text("SINGLE_RESULT").attr('value', "SINGLE_RESULT"));
    selectPropertyStatus.append($('<option>').text("CONFLICT").attr('value', "CONFLICT"));
    selectPropertyStatus.append($('<option>').text("RESOLVED").attr('value', "RESOLVED"));
    selectPropertyStatus[0].value = pfc.propertystatus;

    //Property strictness

    holderPFC.append('<text>Strict values: </text>');
    if (pfc.strict){
        var checkPropertyStrictness = $(' <input type="checkbox" id="checkPropertyStrictness" checked>').appendTo(holderPFC);
    } else {
        var checkPropertyStrictness = $(' <input type="checkbox" id="checkPropertyStrictness">').appendTo(holderPFC);
    }

    //Property sourced values

    var sources = getSources();
    var holderPropertySourcedValues = $('<div id="holderSourcedValues">').appendTo(holderPFC);
    selectPropertyName.change(function () {
        renderSourcedValues(this, holderPropertySourcedValues, sources);
    });

    var propertyValues = getPropertyValues(pfc.propertyname);
    var propertyValues = propertyValues.values;
    for (var i in pfc.sourcedvalues) {
        var sv = pfc.sourcedvalues[i];
        var holderSourcedValue = $('<div id="holderSourcedValue">').appendTo(holderPropertySourcedValues);
        holderSourcedValue.append('<text>Property value: </text>');

        var selectPropertyValue = $('<select id="selectPropertyValue">').appendTo(holderSourcedValue);
        holderSourcedValue.append('<text>Property value source: </text>');

        var selectPropertyValueSource = $('<select id="selectPropertyValueSource">').appendTo(holderSourcedValue);
        holderSourcedValue.append('<br>');


        selectPropertyValueSource.append($('<option>').text("").attr('value', ''));
        for (var k in sources){
            var source=sources[k];
            selectPropertyValueSource.append($('<option>').text(source).attr('value', source));
        }
        selectPropertyValueSource[0].value = sv.propertyvaluesource;

        selectPropertyValue.change(function() {
        	showValuesSelect(holderPropertySourcedValues, propertyValues, sources);
        });
        selectPropertyValue.append($('<option>').text("").attr('value', ''));
        for (var m in propertyValues){
            var propertyvalue=propertyValues[m];
            selectPropertyValue.append($('<option>').text(propertyvalue).attr('value', propertyvalue));
        }
        selectPropertyValue[0].value = sv.propertyvalue;
    }
};


function getSources() {
    startSpinner();
    var result = [];
    $.ajax({
        headers: {
            Accept: "application/json; charset=utf-8",
        },
        type: 'GET',
        url: '/c3po/filter/sources',
        async: false,
        success: function (oData) {
            stopSpinner();
            result = oData;
        }
    });
    return result;
};
function getPropertyInfo(propertyName) {
    var collection = "";
    $.ajax({
        type: 'GET',
        async: false,
        url: '/c3po/settings?key=current.collection',
        success: function (oData) {
            collection = oData;
        }
    });
    var result = {};
    var url = '/c3po/filter/values?filter=' + propertyName + '&collection=' + collection;
    $.ajax({
        type: 'GET',
        async: false,
        url: '/c3po/property?name=' + property,
        success: function (oData) {
            result = oData;
        }
    });
    return result;
};
function getPropertyValues(propertyName) {
    startSpinner();


    var collection = "";
    $.ajax({
        type: 'GET',
        async: false,
        url: '/c3po/settings?key=current.collection',
        success: function (oData) {
            collection = oData;
        }
    });
    var result = {};
    var url = '/c3po/filter/values?filter=' + propertyName + '&collection=' + collection;
    $.ajax({
        type: 'GET',
        url: url,
        async: false,
        success: function (oData) {
            stopSpinner();
            result = oData;
        }
    });
    return result;
};
function getAvailableProperties() {
    var elem = [];
    $.ajax({
        headers: {
            Accept: "application/json; charset=utf-8",
        },
        type: 'GET',
        url: '/c3po/properties',
        async: false,
        success: function (oData) {
            elem = oData;
        }
    });
    return elem;
};


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
$.fn.spin = function (opts) {
    this.each(function () {
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

}

//stop spinner
function stopSpinner() {
    $('#spinner').spin(false);
    $('#spinner').css("opacity", "0");

}


//###############################
//#			  Filter			#
//###############################


function applyFilter() {
    var final = [];
    var pfcs = $('.holderPropertyFilterCondition');
    for (var i = 0; i < pfcs.length; i++) {
        var pfc = pfcs[i];
        var result = {};
        result.propertyname = pfc.querySelector('#selectPropertyName').value;
        result.propertystatus = pfc.querySelector('#selectPropertyStatus').value;
        result.strict=pfc.querySelector('#checkPropertyStrictness').checked;
        result.sourcedvalues = [];
        var sourcedvalues = pfc.querySelector('#holderSourcedValues');
        for (var j = 0; j < sourcedvalues.childNodes.length; j++) {
            var sourcedvalue = sourcedvalues.childNodes[j];
            var sv = {};
            sv.propertyvalue = sourcedvalue.querySelector('#selectPropertyValue').value;
            sv.propertyvaluesource = sourcedvalue.querySelector('#selectPropertyValueSource').value;
            result.sourcedvalues.push(sv);
        }
        final.push(result);
    }
    //alert(JSON.stringify(final));

    $.ajax({
        url: '/c3po/filter/apply',
        type: "POST",
        data: JSON.stringify(final),
        contentType: "application/json",
        success: function (result) {
            // window.location.href = "/c3po/overview"
        }


    });

}


function renderSourcedValues(selectPropertyName, holderPropertySourcedValues, sources) {
    var property = $(selectPropertyName).val();
    if (property) {
        var tmp = $(selectPropertyName).parent()[0].lastChild;
        if (tmp)
            tmp.innerHTML = "";
        //var propertyValues=getPropertyValues(property);
        var collection = "";
        $.ajax({
            type: 'GET',
            async: false,
            url: '/c3po/settings?key=current.collection',
            success: function (oData) {
                collection = oData;
            }
        });

        var url = '/c3po/filter/values?filter=' + property + '&collection=' + collection;
        $.ajax({
            type: 'GET',
            async: false,
            url: '/c3po/property?name=' + property,
            success: function (oData) {
                if (oData.type == 'INTEGER') {
                    showIntegerPropertyDialog('getValuesForProperty("' + url + '")');

                } else {
                    showOtherProperty(url, holderPropertySourcedValues, sources);   //TODO: make this function more separate
                }
            }
        });

    } else {
        $(selectPropertyName).effect("highlight", {color: '#FF1400'}, "slow");
        // if ($(div).children('select').length > 1) {
        //     ($(div).children('select:last')).remove();
        // }
    }


}


function addNewPropertiesSelectHolder(properties) {

    var div = $('.propertyfilter');
    var pfc = $('<div class="holderPropertyFilterCondition">').appendTo(div);
    var deletediv = $('<div class="delete"><a class="red_button" href="#">x</a></div>').appendTo($(pfc));
    deletediv.click(function () {
        $(this).parent().remove();

    });
    pfc.append('<text>Property name: </text>');
    var propertyname = $('<select id="selectPropertyName">').appendTo(pfc);
    propertyname.append($('<option>').text("").attr('value', ''));
    $.each(properties, function (i, value) {
        propertyname.append($('<option>').text(value).attr('value', value));
    });

    pfc.append('<text>Property status: </text>');
    var propertystatus = $('<select id="selectPropertyStatus">').appendTo(pfc);
    propertystatus.append($('<option>').text("").attr('value', ''));
    propertystatus.append($('<option>').text("OK").attr('value', "OK"));
    propertystatus.append($('<option>').text("SINGLE_RESULT").attr('value', "SINGLE_RESULT"));
    propertystatus.append($('<option>').text("CONFLICT").attr('value', "CONFLICT"));
    propertystatus.append($('<option>').text("RESOLVED").attr('value', "RESOLVED"));

    pfc.append('<text>Strict values: </text>');
    var checkPropertyStrictness = $(' <input type="checkbox" id="checkPropertyStrictness">').appendTo(pfc);

    var propertysourcedvalues = $('<div id="holderSourcedValues">').appendTo(pfc);


    var sources = [];
    $.ajax({
        headers: {
            Accept: "application/json; charset=utf-8"
        },
        type: 'GET',
        url: '/c3po/filter/sources',
        async: false,
        success: function (oData) {
            sources = oData;
        }
    });


    // if the select is clicked then get the values for the current
    // option.
    propertyname.change(function () {
        renderSourcedValues(this, propertysourcedvalues, sources);
    });
    //}
}

function showOtherProperty(url, div, sources) {
    startSpinner();
    $.ajax({
        type: 'GET',
        url: url,
        success: function (oData) {
            stopSpinner();
            showValuesSelect(div, oData.values, sources);
        }
    });
}

function showValuesSelect(div, propertyValues, sources) {
    var holderSourcedValue = $('<div id="holderSourcedValue">').appendTo(div);
    holderSourcedValue.append('<text>Property value: </text>');
    var selectPropertyValue = $('<select id="selectPropertyValue">').appendTo(holderSourcedValue);
    holderSourcedValue.append('<text>Property value source: </text>');
    var selectPropertyValueSource = $('<select id="selectPropertyValueSource">').appendTo(holderSourcedValue);
    holderSourcedValue.append('<br>');
    selectPropertyValueSource.append($('<option>').text("").attr('value', ''));
    $.each(sources, function (i, value) {
        selectPropertyValueSource.append($('<option>').text(value).attr('value', value));
    });
    selectPropertyValue.change(function () {
        showValuesSelect(div, propertyValues, sources);
    });
    selectPropertyValue.append($('<option>').text("").attr('value', ''));
    $.each(propertyValues, function (i, value) {
        selectPropertyValue.append($('<option>').text(value).attr('value', value));
    });

}

function showIntegerPropertyDialog(func) {
    $("#overlay").addClass('activeoverlay');
    var popup = $('#filterpopup');
    popup.children('.popupreason').text('This is an integer property and it requires the selection of an algorithm for the histogram bins.');
    popup.children('.popupconfig').append($('<select><option/><option value="fixed">fixed</option><option value="sturge">Sturge\'s</option><option value="sqrt">Square-root choice</option></select>'));
    popup.css({'display': 'block', 'z-index': 11});

    $('.popupconfig select:last').change(function () {

        $('.popupconfig .green_button').remove();
        $('.popupconfig input').remove();
        var val = $(this).val();
        if (val == "") {
            $(this).effect("highlight", {color: '#FF1400'}, "slow");
        } else {
            if (val == "fixed") {
                $('.popupconfig').append($('<input type="text" placeholder="bin width" />'));
            }

            $('.popupconfig').append($('<a class="green_button" href=javascript:' + func + '>Apply</a>'))
        }
    });
}

function addNewFilter() {
    $.ajax({
        headers: {
            Accept: "application/json; charset=utf-8"
        },
        type: 'GET',
        url: '/c3po/properties',
        async: false,
        success: function (oData) {
            addNewPropertiesSelectHolder(oData);
        }
    })

}


function getValuesForProperty(url) {
    url = getUrlForIntegerProperty(url);

    $.ajax({
        type: 'GET',
        url: url,
        success: function (oData) {
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
    url += "&alg=" + alg;

    if (alg == "fixed") {
        width = $('.popupconfig input:first').val();
        url += "&width=" + width;
    }

    return url;
}

function hidePopupDialog() {
    $('#overlay').removeClass('activeoverlay');
    var popup = $('#filterpopup');
    popup.css({'display': 'none'});
    popup.children('.popupconfig').children().remove();
}

function changeSetting(setting, value) {
    $.ajax({
        type: 'POST',
        url: '/c3po/settings?setting=' + setting + '&value=' + value,
        async: false
    });
}

function showValueOptionDialog() {
    $("#overlay").addClass('activeoverlay');

    var popup = $('#filterpopup');
    popup.children('.popupreason').text('This is an integer property and it requires the selection of an algorithm for the histogram bins.');
    popup.children('.popupconfig').append($('<select><option value="fixed">fixed</option><option value="sturge">Sturge\'s</option><option value="sqrt">Square-root choice</option></select>'));
    popup.css({'display': 'block', 'z-index': 11});

    $('.popupconfig select').click(function () {
        $('.popupconfig .green_button').remove();
        $('.popupconfig input').remove();
        if ($(this).val() == "fixed") {
            $('.popupconfig').append($('<input type="text" placeholder="nr of bins" />'));
        }

        $('.popupconfig').append($('<a class="green_button" href="javascript:hideValueOptionDialog(true)">Apply</a>'))
    });
}

function hideValueOptionDialog(apply) {
    $('#overlay').removeClass('activeoverlay');
    var popup = $('#filterpopup');
    popup.css({'display': 'none'});
    $('.popupreason').css({'color': '#888'});
    popup.children('.popupconfig').children().remove();
}

function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}
