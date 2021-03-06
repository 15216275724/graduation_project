package com.cyt.graduation_project.business.controller;

import com.cyt.graduation_project.business.entry.activity.ActivityInfo;
import com.cyt.graduation_project.business.entry.position.Position;
import com.cyt.graduation_project.business.entry.userinfo.User;
import com.cyt.graduation_project.business.service.ActivityService;
import com.cyt.graduation_project.business.service.AddressService;
import com.cyt.graduation_project.sys.util.AddressUtil;
import com.cyt.graduation_project.sys.util.DateFormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@CrossOrigin
@RestController
public class ActivityController {
    @Autowired
    private ActivityService activityService;

    @Autowired
    private AddressService addressService;

    //查询数量
    @RequestMapping(value = "/getActivityNumByCondition", method = RequestMethod.PUT)
    public Object getActivityNumByCondition(int type ,Integer userIdStr,int page, int pageSize, String inputCondition,HttpServletRequest request){
        User user = (User) request.getSession().getAttribute(userIdStr+"");
        int state = -1 ;
        switch (inputCondition){
            case "待审核":
                state = 0;
                break;
            case "已审核":
                state = 1;
                break;
            case "已发布":
                state = 2;
                break;
            case "审核未过":
                state = 3;
                break;
            case "结束":
                state = 4;
                break;
        }
        if(user != null && user.getRoleId() == 2){          //管理员
            if(type !=3){
                if(state != -1){   //按活动状态查询
                    return activityService.getActivityNumByState(state);
                }else {
                    return activityService.getActivityNumByCondition(inputCondition);
                }

            }else{
                return activityService.getPersonalActivityNumyUserId(user.getUserId());
            }
        }else {
            if (user != null && user.getRoleId() == 1) {
                if (type == 3) {
                    return activityService.getPersonalActivityNumyUserId(user.getUserId());
                }
            }
            if(state == 2 || state ==4 ){   //按活动状态查询
                return activityService.getActivityNumByState(state);
            }
            return activityService.queryAllActivityNumPublishedByCondition(inputCondition);
        }


    }
    //通过条件查询活动数据
    @RequestMapping(value = "/getActivityInfoByCondition", method = RequestMethod.PUT)
    public Object getActivityInfoByCondition(int type ,Integer userIdStr,int page, int pageSize, String inputCondition,HttpServletRequest request){
        User user = (User) request.getSession().getAttribute(userIdStr+"");
        int state = -1 ;
        switch (inputCondition){
            case "待审核":
                state = 0;
                break;
            case "已审核":
                state = 1;
                break;
            case "已发布":
                state = 2;
                break;
            case "审核未过":
                state = 3;
                break;
            case "结束":
                state = 4;
                break;
        }
        if(user != null && user.getRoleId() == 2){          //管理员
            if(type !=3){
                if(state != -1){   //按活动状态查询
                    return activityService.getActivityInfoByState(page,pageSize,state);
                }else {
                    return activityService.getActivityInfoByCondition(page,pageSize,inputCondition);
                }
            }else{
                return activityService.getPersonalActivityInfoByUserId(page,pageSize,user.getUserId());
            }
        }else {
            if (user != null && user.getRoleId() == 1) {
                if (type == 3) {
                    return activityService.getPersonalActivityInfoByUserId(page, pageSize, user.getUserId());
                }
            }
            if(state == 2 || state ==4 ){   //按活动状态查询
                return activityService.getActivityInfoByState(page,pageSize,state);
            }
            return activityService.queryAllActivityInfoPublishedByCondition(page,pageSize,inputCondition);
        }


    }


    //查询所有的支教活动信息
    @RequestMapping(value = "/queryAllActivityInfo", method = RequestMethod.PUT)
    public Object queryAllActivityInfo(Integer userIdStr, int page,int pageSize,HttpServletRequest request){
        User user = (User) request.getSession().getAttribute(userIdStr+"");
        if(user != null && user.getRoleId() == 2){   //管理员可以查看所有类型的活动信息
            return activityService.queryAllActivityInfo(page,pageSize);
        }
        //游客只能查看已经发布的志愿活动
        return activityService.queryAllActivityInfoPublished(page,pageSize);

    }


    //首页查询发布的支教活动信息
    @RequestMapping("/queryActivityInfoIndex")
    public Object queryActivityInfoIndex(){
        //游客只能查看已经发布的志愿活动
        return activityService.queryAllActivityInfoPublished(1,15);

    }


    //更新支教活动信息状态
    @RequestMapping(value = "/updateActivityState", method = RequestMethod.PUT)
    public Boolean updateActivityState(int activityId,int reviewStatus){

        return activityService.updateActivityState(activityId,reviewStatus);

    }

    //获取活动数量
    @RequestMapping("/getActivityAccount")
    public Object getActivityAccount(){
        return activityService.getActivityAccount();
    }



    //获取已发布活动数量
    @RequestMapping("/getActivityAccountPublished")
    public Object getActivityAccountPublished(){

        return activityService.getActivityAccountPublished();
    }


    //新增支教活动信息
    @RequestMapping("/addActivityInfo")
    public Object addActivityInfo(@RequestBody ActivityInfo activityInfo, HttpServletRequest request){
        User user = (User) request.getSession().getAttribute(activityInfo.getUserId()+"");
        String flag = "F";
        if(user != null){
            //将时间转换为标准格式
            //activityInfo.setUserId(user.getUserId());
            if(activityInfo.getActivityStartTime() != null){
                activityInfo.setActivityStartTime(DateFormatUtil.dateFormat2(activityInfo.getActivityStartTime()));
                activityInfo.setActivityEndTime(DateFormatUtil.dateFormat2(activityInfo.getActivityEndTime()));
            }else {
                activityInfo.setActivityStartTime(null);
                activityInfo.setActivityEndTime(null);
            }

            activityInfo.setPassTime(null);
            activityInfo = (ActivityInfo) getAddressNameById(activityInfo);
            //activityInfo.setActivityAddress(getAddressNameById(activityInfo.getActivityAddress()));
            //activityInfo.setProvinceName();
            if(activityService.addActivityInfo(activityInfo)){
                flag = "S";
            }
        }

        return flag;
    }

    //通过活动id修改支教活动信息（未审核之前可以修改）
    @RequestMapping("/editActivityInfoById")
    public Object editActivityInfoById(@RequestBody ActivityInfo activityInfo){
        System.out.println(activityInfo.toString());
        String flag = "F";
        if(activityInfo.getActivityStartTime() != null){
            activityInfo.setActivityStartTime(DateFormatUtil.dateFormat2(activityInfo.getActivityStartTime()));
            activityInfo.setActivityEndTime(DateFormatUtil.dateFormat2(activityInfo.getActivityEndTime()));
        }else {
            activityInfo.setActivityStartTime(null);
            activityInfo.setActivityEndTime(null);
        }

        activityInfo.setPassTime(null);
        if(activityService.editActivityInfoById(activityInfo)){
            flag = "S";
        }
        return flag;
    }

    //通过活动id删除支教活动信息（未审核之前可以删除 0表示未审核）
    @RequestMapping(value = "/deleteActivityInfoById", method = RequestMethod.PUT)
    public Object deleteActivityInfoById(int activityId,Integer userIdStr,HttpServletRequest request){
        System.out.println(userIdStr);
        User user = (User) request.getSession().getAttribute(userIdStr+"");
        String flag = "F";

        if(user != null && user.getRoleId() == 2){
            if(activityService.deleteActivity(activityId)){
                flag = "S";
            }
        }else {
            if(activityService.deleteActivityInfoById(activityId)){
                flag = "S";
            }
        }


        return flag;
    }

    //通过活动id查找活动信息
    @RequestMapping("/queryActivityInfoByActivityId")
    public Object queryActivityInfoByActivityId(ActivityInfo activityInfo){
        return activityService.queryActivityInfoByActivityId(activityInfo);
    }


    //查询已经登录用户申请的志愿活动数量
    @RequestMapping(value = "/queryActivityNumByUserLoging",method = RequestMethod.PUT)
    public Object queryActivityNumByUserLoging(HttpServletRequest request,Integer userIdStr){

        User user = (User) request.getSession().getAttribute(userIdStr+"");
        if(user != null){
            return activityService.queryActivityNumByUserLoging(user.getUserId());
        }else{
            return 0;
        }

    }

    /*//通过用户id查找活动信息
    @RequestMapping(value = "/queryActivityInfoByUserId", method = RequestMethod.PUT)
    public Object queryActivityInfoByUserId(int page, int pageSize, int inputCondition){
        return activityService.queryActivityInfoByUserId(page,pageSize,inputCondition);
    }*/

    //查询对应的省份申报活动的数量
    @RequestMapping(value = "/getActivityCountByProvinceName", method = RequestMethod.PUT)
    public Object getActivityCountByProvinceName(String year){
        if(year != null && year != ""){
            year = (Integer.parseInt(year.substring(0,4)) + 1)+"";
        }
        return activityService.getActivityCountByProvinceName(year);
    }

    //通过接收前台的地址id查询地址的name
    @Transactional
    public Object getAddressNameById(ActivityInfo activityInfo){
        //后台接收的值未id-id-id格式
        String add = activityInfo.getActivityAddress();
        String tempAdd[] = add.split(":"); //将前台的具体地址截取
        String addName[] = tempAdd[0].split("-");

        String lastAdress = "";

        //输入的地址进行判断
        Position position = (Position) AddressUtil.returnAddress(addName);

        String provinceName = addressService.getProvinceNameById(position.getProvinceId());
        String cityName = addressService.getCityNameById(position.getCityId());
        String countyName = addressService.getCountyNameById(position.getCountyId());
        String townName = addressService.getTownNameById(position.getTownId());
        String villageName = addressService.getVillageNameById(position.getVillageId());
        if(provinceName == null){
            position.setProvinceName("");
        }else {
            position.setProvinceName(provinceName);
        }

        if(cityName == null){
            position.setCityName("");
        }else {
            position.setCityName(cityName);
        }

        if(countyName == null){
            position.setCountyName("");
        }else {
            position.setCountyName(countyName);
        }

        if(townName == null){
            position.setTownName("");
        }else {
            position.setTownName(townName);
        }

        if(villageName == null){
            position.setVillageName("");
        }else {
            position.setVillageName(villageName);
        }

        if(tempAdd.length == 2 && tempAdd[1] != ""&&tempAdd[1] != null){
            lastAdress =position.getProvinceName()+" "+position.getCityName()+" "
                    +position.getCountyName()+" "+position.getTownName()+" "+position.getVillageName()
                    +" "+ tempAdd[1];
        }else{
            lastAdress =position.getProvinceName()+" "+position.getCityName()+" "
                    +position.getCountyName()+" "+position.getTownName()+" "+position.getVillageName();
        }

        activityInfo.setProvinceName(position.getProvinceName());
        activityInfo.setActivityAddress(lastAdress);

        return activityInfo;
    }
    //预测各省支教活动数量
    @RequestMapping(value = "/AnalysisData",method = RequestMethod.PUT)
    public  Object getProvinctNumByYear(String year){
        //前台传到后台年份会少1
         return activityService.getProvinctNumByYear(year);

    }
    @RequestMapping(value = "/getYear")
    public Object getYear(){
        return activityService.getYear();
    }


    @RequestMapping(value = "/getActivityAccountByInput")
    public  Object getActivityAccountByInput(String input,HttpServletRequest request){

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("userInfo");
        if(user!=null){
            if(user.getRoleId() == 2){
                return activityService.getActivityAccountByInput(input);
            }
        }else {
            return activityService.getActivityAccountByInput2(input);
        }
        return activityService.getActivityAccountByInput2(input);

    }

    //查看个人申请的支教活动
    @RequestMapping(value = "/searchPersonalByCondition",method = RequestMethod.PUT)
    public Object searchPersonalByCondition(Integer userIdStr,int page, int pageSize){
        return activityService.getPersonalActivityInfoByUserId(page,pageSize,userIdStr);
    }


}
