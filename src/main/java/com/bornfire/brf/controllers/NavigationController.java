package com.bornfire.brf.controllers;

import java.io.File;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;


import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.brf.entities.AccessAndRoles;
import com.bornfire.brf.entities.AccessandRolesRepository;
import com.bornfire.brf.entities.CBUAE_BRF1_1_DETAIL_ENTITY;
import com.bornfire.brf.entities.CBUAE_BRF1_1_DETAIL_Repo;
import com.bornfire.brf.entities.RRReport;
import com.bornfire.brf.entities.RT_BankNameMaster;
import com.bornfire.brf.entities.RT_BankNameMasterRepository;
import com.bornfire.brf.entities.RT_CountryRiskDropdown;
import com.bornfire.brf.entities.RT_CountryRiskDropdownRepo;
import com.bornfire.brf.entities.RT_DataControl;
import com.bornfire.brf.entities.RT_DatacontrolRepository;
import com.bornfire.brf.entities.RT_NostroAccBalData;
import com.bornfire.brf.entities.RT_NostroAccBalDataRepository;
import com.bornfire.brf.entities.Request_code_mapping_Entity;
import com.bornfire.brf.entities.Request_code_mapping_Repo;
import com.bornfire.brf.entities.STD_Demo_Entity;
import com.bornfire.brf.entities.STD_Demo_Repo;
import com.bornfire.brf.entities.UserProfile;
import com.bornfire.brf.entities.UserProfileRep;
import com.bornfire.brf.entities.RRReportRepo;
import com.bornfire.brf.entities.CBUAE_BRFValidationsRepo;


import com.bornfire.brf.services.AccessAndRolesServices;
import com.bornfire.brf.services.LoginServices;
import com.bornfire.brf.services.NostroAccBalDataService;
import com.bornfire.brf.services.RT_DataControlService;
import com.bornfire.brf.services.Request_code_mapping_service;
import com.bornfire.brf.services.crud_operations;

@Controller
@ConfigurationProperties("default")
public class NavigationController {

	private static final Logger logger = LoggerFactory.getLogger(NavigationController.class);
	/*
	 * @PersistenceContext private EntityManager entityManager;
	 */

	UserProfileRep UserProfileReps;
	
	@Autowired
	CBUAE_BRF1_1_DETAIL_Repo BRF1_1_DETAIL_Repo;
	
	@Autowired
	CBUAE_BRFValidationsRepo cbuae_brfvalidationsRepo;
	@Autowired
	RRReportRepo rrReportlist;

	@Autowired
	LoginServices loginServices;
	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	AccessAndRolesServices AccessRoleService;

	@Autowired
	AccessandRolesRepository accessandrolesrepository;

	@Autowired
	RT_NostroAccBalDataRepository nostroAccBalRepo;

	@Autowired
	NostroAccBalDataService nostroService;

	@Autowired
	RT_DataControlService RT_DataControlService;

	@Autowired
	RT_DatacontrolRepository RT_DatacontrolRepository;

	@Autowired
	RT_BankNameMasterRepository bankRepo;

	@Autowired
	RT_CountryRiskDropdownRepo countryRepo;
	
	@Autowired
	STD_Demo_Repo std_demo_repo; 
	
	@Autowired
	crud_operations crud_operation;
	
	@Autowired
	Request_code_mapping_Repo request_code_mapping_repo ;
	
	@Autowired
	Request_code_mapping_service request_code_mapping_service;

	private String pagesize;

	public String getPagesize() {
		return pagesize;
	}

	public void setPagesize(String pagesize) {
		this.pagesize = pagesize;
	}

	@RequestMapping(value = "Dashboard", method = { RequestMethod.GET, RequestMethod.POST })
	public String dashboard(Model md, HttpServletRequest req) {

		String domainid = (String) req.getSession().getAttribute("DOMAINID");
		String userid = (String) req.getSession().getAttribute("USERID");

		md.addAttribute("changepassword", loginServices.checkPasswordChangeReq(userid));
		md.addAttribute("checkpassExpiry", loginServices.checkpassexpirty(userid));
		md.addAttribute("checkAcctExpiry", loginServices.checkAcctexpirty(userid));
		int completed = 0;
		int uncompleted = 0;

		md.addAttribute("completed", completed);
		md.addAttribute("uncompleted", uncompleted);
		md.addAttribute("menu", "Dashboard");
		return "Dashboard";
	}

	@RequestMapping(value = "AccessandRoles", method = { RequestMethod.GET, RequestMethod.POST })
	public String IPSAccessandRoles(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, @RequestParam(required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size, Model md, HttpServletRequest req) {

		String roleId = (String) req.getSession().getAttribute("ROLEID");
		System.out.println("role id is : " + roleId);
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("menu", "ACCESS AND ROLES");
			md.addAttribute("menuname", "ACCESS AND ROLES");
			md.addAttribute("formmode", "list");
			md.addAttribute("AccessandRoles", accessandrolesrepository.rulelist());
		} else if (formmode.equals("add")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - ADD");
			md.addAttribute("formmode", "add");
		} else if (formmode.equals("edit")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - EDIT");
			md.addAttribute("formmode", formmode);
			md.addAttribute("IPSAccessRole", AccessRoleService.getRoleId(userid));
		} else if (formmode.equals("view")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - INQUIRY");
			md.addAttribute("formmode", formmode);
			md.addAttribute("IPSAccessRole", AccessRoleService.getRoleId(userid));

		} else if (formmode.equals("verify")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - VERIFY");
			md.addAttribute("formmode", formmode);
			md.addAttribute("IPSAccessRole", AccessRoleService.getRoleId(userid));

		} else if (formmode.equals("delete")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - DELETE");
			md.addAttribute("formmode", formmode);
			md.addAttribute("IPSAccessRole", AccessRoleService.getRoleId(userid));
		}

		md.addAttribute("adminflag", "adminflag");
		md.addAttribute("userprofileflag", "userprofileflag");

		return "AccessandRoles";
	}

	@RequestMapping(value = "createAccessRole", method = RequestMethod.POST)
	@ResponseBody
	public String createAccessRoleEn(@RequestParam("formmode") String formmode,
			@RequestParam(value = "adminValue", required = false) String adminValue,
			@RequestParam(value = "RT_ReportsValue", required = false) String RT_ReportsValue,
			@RequestParam(value = "finalString", required = false) String finalString,
			@ModelAttribute AccessAndRoles alertparam, Model md, HttpServletRequest rq) {

		System.out.println("came to controller");
		String userid = (String) rq.getSession().getAttribute("USERID");
		String roleId = (String) rq.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));

		String msg = AccessRoleService.addPARAMETER(alertparam, formmode, adminValue, RT_ReportsValue, finalString,
				userid);

		return msg;
	}

	@RequestMapping(value = "UserProfile", method = { RequestMethod.GET, RequestMethod.POST })
	public String userprofile(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid,
			@RequestParam(value = "page", required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size, Model md, HttpServletRequest req) {

		int currentPage = page.orElse(0);
		int pageSize = size.orElse(Integer.parseInt(pagesize));

		String loginuserid = (String) req.getSession().getAttribute("USERID");
		String WORKCLASSAC = (String) req.getSession().getAttribute("WORKCLASS");
		String ROLEIDAC = (String) req.getSession().getAttribute("ROLEID");
		md.addAttribute("RuleIDType", accessandrolesrepository.roleidtype());

		System.out.println("work class is : " + WORKCLASSAC);
		// Logging Navigation
		loginServices.SessionLogging("USERPROFILE", "M2", req.getSession().getId(), loginuserid, req.getRemoteAddr(),
				"ACTIVE");
		Session hs1 = sessionFactory.getCurrentSession();
		md.addAttribute("menu", "USER PROFILE"); // To highlight the menu

		if (formmode == null || formmode.equals("list")) {

			md.addAttribute("formmode", "list");// to set which form - valid values are "edit" , "add" & "list"
			md.addAttribute("WORKCLASSAC", WORKCLASSAC);
			md.addAttribute("ROLEIDAC", ROLEIDAC);
			md.addAttribute("loginuserid", loginuserid);

			Iterable<UserProfile> user = loginServices.getUsersList();

			md.addAttribute("userProfiles", user);

		} else if (formmode.equals("edit")) {

			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", loginServices.getUser(userid));

		} else if (formmode.equals("add")) {
			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", loginServices.getUser(""));
		} else if (formmode.equals("verify")) {

			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", loginServices.getUser(userid));

		} else {

			md.addAttribute("formmode", formmode);
			md.addAttribute("FinUserProfiles", loginServices.getFinUsersList());
			md.addAttribute("userProfile", loginServices.getUser(""));

		}

		return "Userprofile";
	}

	@GetMapping("/getRoleDetails")
	@ResponseBody
	public AccessAndRoles getRoleDetails(@RequestParam String roleId) {
		System.out.println("role id for fetching is : " + roleId);
		return accessandrolesrepository.findById(roleId).orElse(null);
	}

	@RequestMapping(value = "createUser", method = RequestMethod.POST)
	@ResponseBody
	public String createUser(@RequestParam("formmode") String formmode, @ModelAttribute UserProfile userprofile,
			Model md, HttpServletRequest rq) {

		String mob = (String) rq.getSession().getAttribute("MOBILENUMBER");
		String role = (String) rq.getSession().getAttribute("ROLEDESC");
		String userId = (String) rq.getSession().getAttribute("USERID");
		String userName = (String) rq.getSession().getAttribute("USERNAME");
		String msg = loginServices.addUser(userprofile, formmode, userId, userName, mob, role);

		return msg;
	}

	@RequestMapping(value = "deleteuser", method = RequestMethod.POST)
	@ResponseBody
	public String deleteuser(@RequestParam("formmode") String userid, Model md, HttpServletRequest rq) {

		String msg = loginServices.deleteuser(userid);

		return msg;

	}

	// In Reports
	// In Modify section If we select the Bank name other details are fetching code
	@RequestMapping(value = "getBankDetails", method = RequestMethod.GET)
	public ResponseEntity<Map<String, String>> getBankDetails(@RequestParam String bankName) {

		RT_BankNameMaster bank = bankRepo.findByBankName(bankName);
		System.out.println("came to controller with bank name ");
		if (bank == null) {
			return ResponseEntity.notFound().build();
		}

		Map<String, String> bankDetails = new HashMap<>();
		bankDetails.put("bankSymbol", bank.getBankSymbol());
		bankDetails.put("conventionalIslamic", bank.getConventionalIslamic());
		bankDetails.put("localForeign", bank.getLocalForeign());
		bankDetails.put("cbuaeTiering", bank.getCbuaeTiering());

		return ResponseEntity.ok(bankDetails);
	}

	// In Modify section If we select the Country of Risk, cbuaeGeographicalZone
	// fetching code
	@RequestMapping(value = "getCountryDetails", method = RequestMethod.GET)
	public ResponseEntity<Map<String, String>> getCountryDetails(@RequestParam String countryName) {
		RT_CountryRiskDropdown country = countryRepo.findByCountryOfRisk(countryName);
		System.out.println("Fetched country details for: " + countryName);

		if (country == null) {
			return ResponseEntity.notFound().build();
		}

		Map<String, String> response = new HashMap<>();
		response.put("cbuaeGeographicalZone", country.getCbuaeGeographicalZone());

		return ResponseEntity.ok(response);
	}

	// Creating Data Control
	@PostMapping("/createDataControl")
	@ResponseBody
	public String createDataControl(
			@RequestParam(name = "formmode", required = false, defaultValue = "add") String formmode,
			@RequestParam(name = "report_name", required = false, defaultValue = "add") String report_name,
			@ModelAttribute RT_DataControl dto) {
		System.out.println("Controller: createOrUpdateNostro() called");
		System.out.println("report name is: " + report_name);
		return RT_DataControlService.createOrUpdate(dto, formmode, report_name);
	}

	// Nostro Report Codes Given Below
	@RequestMapping(value = "Nostro_Account_Bal", method = RequestMethod.GET)
	public String NostroAccountBal(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String accountNo, Model md, HttpServletRequest req) {

		if ("edit".equalsIgnoreCase(formmode) && accountNo != null && !accountNo.isEmpty()) {
			RT_NostroAccBalData data = nostroAccBalRepo.findById(accountNo).orElse(null);
			md.addAttribute("nostroData", data);
			System.out.println("edit is formmode");
			md.addAttribute("formmode", "edit");
		} else if ("list".equalsIgnoreCase(formmode)) {
			md.addAttribute("branchList", nostroAccBalRepo.getlist());
			System.out.println("list is formmode");
			md.addAttribute("formmode", "list");
		} else {
			md.addAttribute("formmode", "add");
			md.addAttribute("formmode", "null");
		}

		List<RT_BankNameMaster> bankList = bankRepo.findAllByOrderByBankNameAsc();
		List<RT_CountryRiskDropdown> countryList = countryRepo.findAllByOrderByCountryOfRiskAsc();

		md.addAttribute("bankList", bankList);
		md.addAttribute("countryList", countryList);

		return "RT/Nostro_Account_Bal";
	}

	// Update Submit code in nostro
	@RequestMapping("/updateNostro")
	@ResponseBody
	public String updateNostro(@ModelAttribute RT_NostroAccBalData nostroData, HttpServletRequest req) {

		boolean updated = nostroService.updateNostro(nostroData);
		System.out.println("msg is : " + updated);

		if (updated) {
			System.out.println("Update successful");
			return "Updated successful";
		} else {
			System.out.println("Update Record not found for update");
			return "Record not found for update";
		}

	}

	// For download excel for downloadNostroExcel

	@RequestMapping(value = "downloadNostroExcel", method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadNostroExcel() throws IOException {
		System.out.println("Entered controller downloadNostroExcel");

		File excelFile = nostroService.generateNostroExcel();
		byte[] excelData = java.nio.file.Files.readAllBytes(excelFile.toPath());

		HttpHeaders headersResponse = new HttpHeaders();
		headersResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headersResponse.setContentDispositionFormData("attachment", "NostroAccBalance.xls");

		return ResponseEntity.ok().headers(headersResponse).body(excelData);
	}


	@GetMapping("/checkDomainFlag")
	@ResponseBody
	public ResponseEntity<String> checkDomainFlag(@RequestParam String rptcode) {
		Optional<RRReport> report = rrReportlist.getParticularReport3(rptcode);

		if (report.isPresent()) {
			String domain = report.get().getDomain(); // Add getter in entity if not already
			if ("Y".equalsIgnoreCase(domain)) {
				return ResponseEntity.ok("ENABLED");
			} else {
				return ResponseEntity.ok("DISABLED");
			}
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
		}
	}
	
	  @RequestMapping(value = "Monthly-1", method = { RequestMethod.GET,RequestMethod.POST })
	  public String monthly1(Model md, HttpServletRequest req)
	  {
	//String roleId = (String) req.getSession().getAttribute("ROLEID");
	  //String domainid = (String) req.getSession().getAttribute("DOMAINID");
	 // md.addAttribute("menu", "Monthly 1 - BRF Report");
	System.out.println("count"+rrReportlist.getReportListmonthly1().size());
	  md.addAttribute("reportlist", rrReportlist.getReportListmonthly1());
	  
	  return "BRF/RRReports";
	  
	  }
	 

	@RequestMapping(value = "BRFValidations", method = { RequestMethod.GET, RequestMethod.POST })
	public String BRFValidations(Model md, @RequestParam(value = "rptcode", required = false) String rptcode,
			@RequestParam(value = "todate", required = false) String todate, HttpServletRequest req) {
		String roleId = (String) req.getSession().getAttribute("ROLEID");

		// md.addAttribute("reportvalue", "RBS Reports");
		// md.addAttribute("reportid", "RBSReports");

		String domainid = (String) req.getSession().getAttribute("DOMAINID");
		// md.addAttribute("reportsflag", "reportsflag");
		// md.addAttribute("menu", "RBS Data Maintenance");

		md.addAttribute("reportlist", cbuae_brfvalidationsRepo.getValidationList(rptcode));
		md.addAttribute("reportlist1", rrReportlist.getReportbyrptcode(rptcode));
		md.addAttribute("RoleId", roleId);

		// md.addAttribute("rpt_date", todate);
		return "BRFValidations";
	}
	@RequestMapping(value = "Demo", method = { RequestMethod.GET, RequestMethod.POST })
	public String Demo(Model md,@RequestParam(required = false) String formmode,@RequestParam(required = false) BigDecimal srl_no,HttpServletRequest req)
	{
		if(formmode==null || formmode.equals("List") ) {
			List<STD_Demo_Entity> List_Demo=std_demo_repo.getListbydate();
			System.out.println("srl_no:"+List_Demo.get(0).getSrl_no());
			
			md.addAttribute("demoattribute",List_Demo);
			md.addAttribute("formmode","List");
		}
		else if(formmode.equals("add")){
			md.addAttribute("formmode","add");
			
		}
		else if(formmode.equals("edit")){
			md.addAttribute("formmode","edit");
			
			md.addAttribute("demoattribute",crud_operation.getData(srl_no));
			
		}
		else if(formmode.equals("deletedata")){
			md.addAttribute("formmode","deletedata");
		}
		
		return "Demo";
		
	}
	@RequestMapping(value = "add", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String add(@ModelAttribute STD_Demo_Entity STD_Demo_Entity, HttpServletRequest req) {
		return crud_operation.add(STD_Demo_Entity);
	}
	
	
	@RequestMapping(value = "deletedata", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String deletedata(@RequestParam("srl_no") BigDecimal srl_no, HttpServletRequest req) {
	    crud_operation.deletedata(srl_no);
	    return "deleted";
	}
	
	
	@RequestMapping(value = "RequestCodeMapping", method = { RequestMethod.GET, RequestMethod.POST })
	public String Requestcodemapping(Model md,@RequestParam(required = false) String formmode,@RequestParam( required = false) String keyword,HttpServletRequest req) {
		
		if(formmode==null || formmode.equals("List") ) {
			List<Request_code_mapping_Entity> RCM_List=request_code_mapping_repo.getListbydate();
			System.out.println("Cust_id:"+RCM_List.get(0).getCustomer_Id());
			
			md.addAttribute("rcmattribute",RCM_List);
			md.addAttribute("formmode","List");
		}
		else if(formmode.equals("MAList")) {
			List<Request_code_mapping_Entity> MA_List=request_code_mapping_repo.getListbyMAList();
			md.addAttribute("rcmattribute",MA_List);
			md.addAttribute("formmode","MAList");
		}
		else if(formmode.equals("UMAList")) {			
			List<Request_code_mapping_Entity> UMA_List=request_code_mapping_repo.getListbyUMAList();
			System.out.println("Cust_id:"+UMA_List.get(0).getCustomer_Id());
			md.addAttribute("rcmattribute",UMA_List);
			md.addAttribute("formmode","UMAList");
		}
		else if(formmode.equals("addcustomer")){
			md.addAttribute("formmode","addcustomer");
			
		}	
		else if(formmode.equals("search")) {
			System.out.println(keyword);
		    List<Request_code_mapping_Entity> slist = request_code_mapping_repo.searchByKeyword(keyword);
		    md.addAttribute("rcmattribute", slist);
		}
		
		else if(formmode.equals("masearch")) {
			System.out.println(keyword);
		    List<Request_code_mapping_Entity> malist = request_code_mapping_repo.searchByKeywordMA(keyword);
		    md.addAttribute("rcmattribute", malist);
		    md.addAttribute("action", "searchma");
		}
		
		else if(formmode.equals("umasearch")) {
			System.out.println(keyword);
		    List<Request_code_mapping_Entity> umalist = request_code_mapping_repo.searchByKeywordUMA(keyword);
		    md.addAttribute("rcmattribute", umalist);
		    md.addAttribute("action", "searchuma");
		}
		
		
		return "RequestCodeMapping";
	
	}
	@RequestMapping(value = "getdata", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public Request_code_mapping_Entity getdata(@RequestParam String id) {
	    System.out.println("Fetching ID: " + id);
	    Request_code_mapping_Entity entity = request_code_mapping_repo.findById(id).orElse(null);
	    System.out.println("Entity: " + entity);
	    return entity;
	}
	
	@RequestMapping(value = "addcustomer", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String addcustomer(@ModelAttribute Request_code_mapping_Entity Request_code_mapping_Entity, HttpServletRequest req) {
		return request_code_mapping_service.add(Request_code_mapping_Entity);
	}
	
	@RequestMapping(value = "downloadcustomer", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public void downloadcustomer(@ModelAttribute Request_code_mapping_Entity requestEntity,HttpServletResponse response,@RequestParam("keyword") String keyword) {
	    try {
	        byte[] excelData = request_code_mapping_service.generateExcel(keyword);
	        response.setHeader("Content-Disposition", "attachment; filename=Request_Code_Mapping.xlsx");
	        response.getOutputStream().write(excelData);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	   

}
