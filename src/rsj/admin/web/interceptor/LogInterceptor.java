package rsj.admin.web.interceptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.StrutsStatics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rsj.admin.web.bean.UserSessionBean;
import rsj.admin.web.constant.Global;
import rsj.admin.web.domain.log.Log;
import rsj.admin.web.domain.user.Permission;
import rsj.admin.web.domain.user.PermissionItem;
import rsj.admin.web.enums.LogType;
import rsj.admin.web.service.log.LogService;
import rsj.admin.web.utils.StringUtil;

import com.lehecai.core.util.CoreHttpUtils;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class LogInterceptor extends AbstractInterceptor {
	private static final long serialVersionUID = 5545476931270525263L;
	private Logger logger = LoggerFactory.getLogger(LogInterceptor.class);
	
	private static Set<String> actionSet = new HashSet<String>();//必须记录日志的action
	
	static {
		actionSet.add("member");
	}
	private LogService logService;
	
	@SuppressWarnings("unchecked")
	public String intercept(ActionInvocation invocation) throws Exception{	
		ActionContext ac = invocation.getInvocationContext();
		HttpServletRequest request= (HttpServletRequest) ac.get(StrutsStatics.HTTP_REQUEST);  
		String actionName = ac.getName();
		Map paramMap = ac.getParameters();
		
		logger.info("method：{}，action：{}", paramMap.get("action"), actionName);
		
		boolean isLog = true;
		if (!actionSet.contains(actionName)) {
			String[] actions = (String[]) paramMap.get("action");
			if (actions != null && actions.length != 0) {		//传递action参数
				String action = actions[0];
				logger.info("action：{}", action);
				Pattern p = Pattern.compile("\\w*view\\w*|get\\w*|\\w*list\\w*|search\\w*|find\\w*|\\w*status|query\\w*");
				if (p.matcher(action.toLowerCase()).matches()) {
					isLog = false;
				}
			} else {											//不传递action参数即handle方法
				isLog = false;
			}
		}
 		String result = "/";
		if (isLog) {		//需要记录日志信息
			UserSessionBean userSessionBean = (UserSessionBean) ac.getSession().get(Global.USER_SESSION);
			Log log = new Log();
			if(userSessionBean != null) {
				log.setUserName(userSessionBean.getUser().getUserName());
				log.setName(userSessionBean.getUser().getName());
			}
			String message = StringUtil.getStringByActionHashMap(paramMap);
			if(message != null && message.length() >= 1000){
				message = message.substring(0,900);
			}
			log.setUrl(request.getRequestURL().toString());
			log.setActionName(actionName);
			log.setParams(message);
			log.setRemoteIP(getRemoteIp(request));
			log.setLogType(LogType.OPERATIONTYPE);
			
			try {
				result = invocation.invoke();
			} catch (Exception e) {
				log.setLogType(LogType.OPERATIONTYPE);
				log.setParams(e.getMessage());
			}
			logService.save(log);
		} else {		//不需要记录日志信息
			try {
				result = invocation.invoke();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}
	
	//获取访问者IP
	private String getRemoteIp(HttpServletRequest request){
		return CoreHttpUtils.getClientIP(request);
	}
	
	public List<PermissionItem> getSelfPermissionItems(Permission permission, List<PermissionItem> permissionItems){
		List<PermissionItem> list = new ArrayList<PermissionItem>();
		for(PermissionItem permissionItem : permissionItems){
			if(permissionItem != null && permissionItem.getPermissionID().longValue() ==  permission.getId().longValue()){
				list.add(permissionItem);
			}
		}
		return list;
	}
	
	public LogService getLogService() {
		return logService;
	}
	public void setLogService(LogService logService) {
		this.logService = logService;
	}
}
