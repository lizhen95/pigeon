<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <link href="/ztree/ztree.css" rel="stylesheet" type="text/css"/>
  <link href="/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
  <link href="/jquery/jquery-ui.css" rel="stylesheet" type="text/css"/>
  <script src="/jquery/jquery-1.7.2.min.js"></script>
  <script src="/jquery/jquery-ui.js"></script>
  <script src="/jquery/jquery.easy-confirm-dialog.js"></script>
  <script src="/ztree/jquery.ztree.core-3.3.min.js"></script>
	<SCRIPT type="text/javascript">
		<!--
		var setting = {
			data: {
				key: {
					title:"title"
				},
				simpleData: {
					enable: true
				},
			},
			callback: {
				onClick: function(e, treeId, treeNode, clickFlag){
					var content = $("#content").empty().show();
					var result = $("#result").empty().hide();
					if(treeNode.pId == null){
						content.hide();
						return;
					}
					if(treeNode.token=='true'){
						content.append("<p>authentication token (timestamp:${timestamp}):<p><input type='text' id='token' value=''/></p>");
					} else if('${validate}'=='true'){
						content.append("<p>verification code(from /data/applogs/pigeon/pigeon.*.log):<p><input type='text' id='token' value=''/></p>");
					}
					if(treeNode.parameters>0){
						content.append("<p>parameter values:</p>");
					}
					for(var i = 0; i < treeNode.parameters; i++){
						content.append("<p><input type='text' value=''/></p>");
					}
                    content.append("<p><select id ='invokeType'><option value='GET'>GET</option><option value='POST'>POST</option></select></p>");
					content.append("<button id='invokeBtn' class='submit'>invoke</button>");
					$("button.submit", content).on("click", function(){
						var pdata = {};
						pdata.parameters = [];
						$("input", content).each(function(){
							if($(this).attr("id")!='token'){
								pdata.parameters.push($(this).val());
							}
						});
						pdata.url = treeNode.data_url;
						pdata.method = treeNode.data_method;
						pdata.parameterTypes = treeNode.data_parameterTypes.split(',');
                        var type = $('#invokeType').val();
						$.ajax({
                            type:type,
							url:"./invoke.json?validate=true&app=${appName}&timestamp=${timestamp}&direct=${direct!"true"}&token=" + $("#token").val(),
							data: pdata,
							success: function(m){
								result.text(JSON.stringify(m)).show();
							},
							error: function(m){
								result.text(JSON.stringify(m)).show();
							}
						});

					});
					if('${validate}'=='true'){
						$("#invokeBtn").easyconfirm({locale: {
							title: 'WARNING',
							text: 'Are you sure you want to continue?',
							button: ['cancel','ok'],
							closeText: 'cancel'
						}});
					}
				}
			}
		};

		var zNodes =[
			<#list services as x>
			{ id:${x_index + 1}, pId:0, name:"${x.name}", title:"${x.type.canonicalName}(published:${x.published})", isParent:true}<#if (x.methods?size>0) >,<#elseif x_has_next>,</#if>
				<#list x.methods as m>
			{ id:${x_index + 1}${m_index + 1}, pId:${x_index + 1}, 
				data_url:"${x.name}",title:"${m.name}(<#list m.parameterTypes as p>${p.canonicalName}<#if p_has_next>,</#if></#list>)",
				data_method:"${m.name}",
				token:"${x.token}",
				data_parameterTypes: "<#list m.parameterTypes as p>${p.name}<#if p_has_next>,</#if></#list>",
				name:"${m.name}(<#list m.parameterTypes as p>${p.canonicalName}<#if p_has_next>,</#if></#list>)", 
				parameters:"${m.parameterTypes?size}"}<#if m_has_next>,<#elseif x_has_next>,</#if>
				</#list>
			</#list> 
		];

		$(document).ready(function(){
			$.fn.zTree.init($("#treeDemo"), setting, zNodes);
		});
		//-->
	</SCRIPT>
</head>
<body style="font-size:62.5%;padding-top:10px;padding-left:10px;">
	<div class="row">
	  <div class="span8"  style="overflow:hidden">
		<div>
		<#if governorUrl != "">
           <p>admin portal: <a href="${governorUrl}" target="_blank">${governorUrl}</a></p>
		</#if>
		<p>pigeon services registered at port ${port}</p>
		<p>version: ${version}</p>
		<p>env: ${environment}</p>
		<p>group: ${group}</p>
		<p>app: ${appName}</p>
		<p>start time: ${startTime}</p>
		</div>
		<div>
			<div class="zTreeDemoBackground left">
				<ul id="treeDemo" class="ztree"></ul>
			</div>
		</div>  
	  </div>
	  <div class="span6">
	  	<div id="content"></div>
	  	<div id='result' class='' style="display:none;border:1px solid #ddd;width:100%;height:100px;"></div>
	  </div>
	</div>

</body>
</html>