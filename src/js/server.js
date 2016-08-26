var http = require('http');
var sha256 = require('js-sha256');
var request = require('request');


function processTripleArray(shaToMatch, tripleArray, fives) {
  /*console.log("array.length = " + tripleArray.length)

  console.log("Hashing to find =", shaToMatch);

  console.log("Triple Array exists =", tripleArray !== null);

  console.log("Fives.length =", fives.length);*/

  var lastSentence = "";

  for(var i = 0, j = tripleArray.length; i < j; i += 1) {
    var triple = tripleArray[i];

    fives.forEach(function (outer) {
      fives.forEach(function (inner) {

        var testSentence = [triple.first, outer, triple.second, inner, triple.third].join(" ");
        if (sha256(testSentence).valueOf() == shaToMatch.valueOf()) {
          return {
            found: true,
            sentence: testSentence
          };
        }
        if ( i == j - 1) {
          lastSentence = testSentence;
        }
      });
    });

  }

  return {
    found: false,
    sentence: lastSentence
  };
}

//server.listen(1337, '127.0.0.1');
var options = {
  host: '0.0.0.0',
  path: '/work',
  port: 4567,
  method: 'GET'
};

var callback = function(response) {
  var str = '', chunkTick = 0;

  //another chunk of data has been received, so append it to `str`
  response.on('data', function (chunk) {
    chunkTick += 1;
    str += chunk;
  });

  //the whole response has been received, so we just print it out here
  response.on('end', function () {
//    console.log("This is the 'end' block.")

    var message = JSON.parse(str);

    var foundObj = processTripleArray(message.toMatch, message.triples, message.fives);

    var requestOptions = {
      url: 'http://0.0.0.0:4567/work',
      method: "POST",
      json: foundObj
    };

    request(requestOptions, function (error, response, body) {
      // Do stuff with response
      if (error) {
        console.error(error);
      } else {
        console.log("Received response from POST");
//        console.log(Object.keys(response), body);
//        console.log(response.body, response.read);
      }
    });
  });


  response.on('error', function (error) {
    console.log("Received error");
    console.log(error);
  });
}

http.request(options, callback)
.on('error', function (error) {
  console.log(error);
})
.on('data', function (data) {
  console.log("This is the data catch")
  console.log(data);
})
.end();

// Easy as pie
// var hash = sha256("many hands make light work")
