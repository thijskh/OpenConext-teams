<%@ page contentType="text/html; charset=UTF-8" %>
<%--
  ~ Copyright 2013 SURFnet bv, The Netherlands
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Licensing Portal</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="description" content="">
  <meta name="author" content="">

  <!-- Le styles -->
  <link href="css/bootstrap.css" rel="stylesheet">
  <style>
    body {
      padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
    }
  </style>
  <link href="css/bootstrap-responsive.css" rel="stylesheet">
</head>

<body>

<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container pull-left">
      <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="brand" href="#">&nbsp;Licensing Portal</a>

      <div class="nav-collapse collapse">
        <ul class="nav">
        </ul>
      </div>
      <!--/.nav-collapse -->
    </div>
  </div>
</div>


<div class="container-fluid">
  <div class="row-fluid">
    <div class="span3">
      <img src="img/logo-surfmarket.png" alt="SURFmarket" style="padding-bottom: 10px"/>
      <div class="well sidebar-nav">
        <ul class="nav nav-list">
          <li class="nav-header">Products</li>
          <li><a href="#">Cloud</a></li>
          <li><a href="#">Physical</a></li>
          <li><a href="#">Imaginary</a></li>
          <li class="nav-header">Licenses</li>
          <li><a href="#">All</a></li>
          <li><a href="#">Expiring</a></li>
          <li class="active"><a href="#">Buy a license</a></li>
          <li><a href="#">By institution</a></li>
          <li><a href="#">By date</a></li>
          <li><a href="#">By product</a></li>
          <li class="nav-header">Reporting</li>
          <li><a href="#">Monthly</a></li>
          <li><a href="#">Administrative</a></li>
          <li><a href="#">Commercial</a></li>
          <li><a href="#">Marketing</a></li>
        </ul>
      </div>
      <!--/.well -->
    </div>
    <!--/span-->
    <div class="span9">
      <div class="row-fluid">
        <div class="span6">

          <h1>Buy a license</h1>
          <br>

          <form action="createGroup.html" method="post">
            <fieldset>
              <legend>Product details</legend>

              <label for="product">Product</label>
              <select name="product" id="product">
                <option>GreenQloud</option>
                <option>BlueQloud</option>
                <option>RedQloud</option>
                <option>YellowQloud</option>
                <option>LaughingOutQloud</option>
              </select>

              <label for="productvariation">Product variation</label>
              <select name="productvariation" id="productvariation">
                <option></option>
                <option>CompuQloud</option>
                <option>StorageQloud</option>
                <option>InstrumentQloud</option>
              </select>

              <label>License type</label>
              <label class="radio">20<input type="radio" checked="checked" id="quantity20" name="quantity" value="20"></label>
              <label class="radio">50<input type="radio" id="quantity50" name="quantity" value="50"></label>
              <label class="radio">100<input type="radio" id="quantity100" name="quantity" value="100"></label>
              </label>
            </fieldset>
            <fieldset>
              <input id="submitbtn" class="btn btn-primary" type="submit" value="Submit"/>
              <input class="btn" type="reset" value="Cancel"/>

            </fieldset>
          </form>
        </div>
        <!--/span-->
      </div>
      <!--/row-->
    </div>
    <!--/span-->
  </div>
  <!--/row-->

  <div class="modal fade" id="postresultmodal">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
          <h4 class="modal-title">Buy license</h4>
        </div>
        <div class="modal-body">
          <p id="postresult"></p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-default" data-dismiss="modal">Ok</button>
        </div>
      </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
  </div><!-- /.modal -->


  <hr>

  <footer>
    <p>&copy; SURFnet 2013</p>
  </footer>

</div>
<!--/.fluid-container-->

<script src="js/jquery-2.0.3.min.js"></script>
<script src="js/bootstrap.min.js"></script>
<script>
  $("form").submit(function() {
//    var submitbtn = $("#submitbtn");
//    submitbtn.attr("disabled", "disabled");
    $.ajax({'type': "post",
      'url': "createGroup.html",
      'data': $("form").serialize()
    }).done(function(data) {
        $( "p#postresult" ).html(data);
        $("#postresultmodal").modal('show');
      }).fail(function(jqxhr, status, err) {
        var responseText = jqxhr.responseText;
        $( "p#postresult" ).html(responseText);
        $("#postresultmodal").modal('show');
      });
//    submitbtn.removeAttr("disabled");
    return false;
  });
</script>

</body>
</html>