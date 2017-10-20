eu_unifiedviews_plugins_transformer_sparql_construct_editor_SparqlEditorComponent = function() {

    // Create the component
    var mycomponent = new mylibrary.MyComponent(this.getElement());

    // Handle changes from the server-side - whenewher the query is changed, this function is called
    this.onStateChange = function() {
        mycomponent.setQuery(this.getState().query);
    };

    // Pass user interaction to the server-side
    var connector = this;
    mycomponent.click = function() {
        connector.onClick(mycomponent.getQuery());
    };

    // this.getq = function() {
    //     connector.onClick(mycomponent.getQuery());
    // }
    //
    // this.setq = function() {
    //     mycomponent.setQuery(this.getState().query);
    // }

}

// Define the namespace
var mylibrary = mylibrary || {};

mylibrary.MyComponent = function (element) {

    var styles = "@import url(' http://cdn.jsdelivr.net/yasqe/2.2/yasqe.min.css ');";
    var newSS=document.createElement('link');
    newSS.rel='stylesheet';
    newSS.href='data:text/css,'+escape(styles);
    document.getElementsByTagName("head")[0].appendChild(newSS);

    element.innerHTML = "<div id=\"yasqe\"></div>"; //<div><input type='button' value='Save'/></div>";


    YASQE.defaults.sparql.showQueryButton = true;
    YASQE.defaults.sparql.endpoint = "http://dbpedia.org/sparql";
    YASQE.defaults.sparql.callbacks.success =  function(data){console.log("success", data);};


    ///adjust YASQE

    /**
     * Gosparqled plugin for YASQE
     */

    // Adds a symbol to the query defining what should be recommended
    var formatQueryForAutocompletion = function(partialToken, query) {
        var cur = yasqe.getCursor(false);
        var begin = yasqe.getRange({line: 0, ch:0}, cur);
        query = begin + "< " + query.substring(begin.length, query.length);
        return query;
    };

    /**
     * Autocompletion function
     */
    var customAutocompletionFunction = function(partialToken, callback) {
        autocompletion.RecommendationQuery(formatQueryForAutocompletion(partialToken, yasqe.getValue()), function(q, type, err) {
            if (err) {
                alert(err)
                return
            }
            if (!q) {
                alert("No recommendation at this position")
                return
            }
            var ajaxConfig = {
                type: "GET",
                crossDomain: true,
                url: sparqled.config.endpoint,
                data: {
                    format: 'application/json',
                    query: q
                },
                success: function(data) {
                    // Get the list of recommended terms
                    var completions = [];
                    for (var i = 0; i < data.results.bindings.length; i++) {
                        var binding = data.results.bindings[i];
                        var pof = binding.POF.value
                        switch (binding.POF.type) {
                            case "typed-literal":
                                pof = "\"" + pof + "\"^^<" + binding.POF["datatype"] + ">";
                                break;
                            case "literal":
                                if (type === autocompletion.PATH) {
                                    // The property path is built as a concatenation
                                    // of URIs' label. It is then typed as a Literal.
                                    break;
                                }
                                if ("xml:lang" in binding.POF) {
                                    pof = "\"" + pof + "\"@" + binding.POF["xml:lang"];
                                } else {
                                    pof = "\"" + pof + "\""
                                }
                                break;
                            case "uri":
                                pof = "<" + pof + ">";
                                break;
                        }
                        completions.push(pof);
                    }
                    callback(completions);
                },
                beforeSend: function(){
                    $('#loading').show();
                },
                complete: function(){
                    $('#loading').hide();
                }
            };
            $.ajax(ajaxConfig);
        })
    };

    /*
     * Plug the recommendation to the YASQE editor
     */

// If token is an uri, return its prefixed form
    var postprocessResourceTokenForCompletion = function(token, suggestedString) {
        if (token.tokenPrefix && token.autocompletionString && token.tokenPrefixUri) {
            // we need to get the suggested string back to prefixed form
            suggestedString = token.tokenPrefix + suggestedString.substring(1 + token.tokenPrefixUri.length, suggestedString.length - 1); // remove wrapping angle brackets
        }
        return suggestedString;
    };

    YASQE.registerAutocompleter("sparqled", function(yasqe) {
        return {
            async : true,
            bulk : false,
            isValidCompletionPosition : function() { return true;  },
            get : customAutocompletionFunction,
            preProcessToken: function(token) {return YASQE.Autocompleters.properties.preProcessToken(yasqe, token)},
            postProcessToken: postprocessResourceTokenForCompletion
        };
    });
    YASQE.defaults.autocompleters = ["prefixes", "variables", "sparqled"];

    ///end

    //finally, initialize YASQE
    var yasqe = YASQE(document.getElementById("yasqe"));

    // Getter and setter for the value property
    this.getQuery = function () {
        return yasqe.getValue();
    };

    this.setQuery = function (value) {
        yasqe.setValue(value);
    };

    // // Set up button click
    // var button = element.getElementsByTagName("input")[0];
    var self = this; // Can't use this inside the function
    // button.onclick = function () {
    //     self.click();
    // };

    //to save the query (change it in the state object) when focus is lost
    var textarea = element.getElementById("yasqe");
    textarea.onblur = function() {
        self.click();
    }


};


