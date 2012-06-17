function eval_clojure(code) {
    var data;
    $.ajax({
        url: "eval.json",
        data: { expr : code },
        async: false,
        success: function(res) { data = res; }
    });
    return data;
}

function html_escape(val) {
    var result = val;
    result = result.replace(/\n/g, "<br/>");
    result = result.replace(/[<]/g, "&lt;");
    result = result.replace(/[>]/g, "&gt;");
    return result;
}

function onValidate(input) {
    return (input != "");
}

function onHandle(line, report) {
    var input = $.trim(line);

    // perform evaluation
    var data = eval_clojure(input);

    // handle error
    if (data.error) {
        return [{msg: data.message, className: "jquery-console-message-error"}];
    }

    // display expr results
    return [{msg: data.result, className: "jquery-console-message-value"}];
}

var controller;

$(document).ready(function() {
    controller = $("#console").console({
        welcomeMessage:'',
        promptLabel: 'isla> ',
        commandValidate: onValidate,
        commandHandle: onHandle,
        autofocus:true,
        animateScroll:true,
        promptHistory:true
    });
});
