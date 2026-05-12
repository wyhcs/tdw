package com.ruoyi.web.controller.system;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.framework.web.service.TokenService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysPostService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
/**
 * 用户信息
 * 
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;

    @Autowired
    private TokenService tokenService;
    /**
     * 获取用户列表
     */
    @Log(title = "用户列表", businessType = BusinessType.EXPORT)
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        List<SysUser> list = new ArrayList<>();
        long total = 0;
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser loginUserInfo = loginUser.getUser();
        // 获取 权限
//        Long loginDeptId = loginUserInfo.getDeptId();
//        SysDept loginSysDept = deptService.selectDeptById(loginDeptId);
//        String loginAncestors = loginSysDept.getAncestors();
//        String[] loginAncestorssplit = loginAncestors.split(",");

        String roleKey = roleService.selectRoleKeyByUserId(loginUserInfo.getUserId());
        startPage();
        if (loginUserInfo.isAdmin()){
            list = userService.selectUserList(user);
//            total = userService.selectUserCount(user);
        } else {
            if (roleKey.contains("platAdmin")){
                list = userService.selectUserListNoAdmin(user);
//                total = userService.selectUserCountNoAdmin(user);
            }else if (roleKey.contains("deptAdmin")){
                user.setUnitId(loginUserInfo.getUnitId());
                list = userService.selectUserListNoAdmin(user);
            }else {
                // 普通用户
                user.setUserId(loginUserInfo.getUserId());
                list = userService.selectUserListNoAdmin(user);
            }
        }
        return getDataTable(list);

    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUser user)
    {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.importTemplateExcel(response, "用户数据");
    }

    /**
     * 根据用户编号获取详细信息
     */
    @GetMapping(value = {"/{userId}" })
    public AjaxResult getInfoByUserId(@PathVariable(value = "userId", required = false) Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (StringUtils.isNotNull(userId))
        {
            userService.checkUserDataScope(userId);
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        return ajax;
    }


    /**
     * 根据用户编号获取详细信息
     */
    @GetMapping(value = { "/"})
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
//        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//        SysUser loginUserInfo = loginUser.getUser();
//        Long loginDeptId = loginUserInfo.getDeptId();
//        // 获取 权限
//        SysDept loginSysDept = deptService.selectDeptById(loginDeptId);
//        String loginAncestors = loginSysDept.getAncestors();
//        String[] loginAncestorssplit = loginAncestors.split(",");
//        if (loginUserInfo.isAdmin()){
//            SysRole role = new SysRole();
//            role.setRoleName("平台");
//            return ajax.put("roles", roleService.selectPlatformRoleList(role));
////            return ajax.put("roles", roleService.selectRoleAll());
//        } else if (loginAncestorssplit.length == 1){
//            SysRole role = new SysRole();
//            role.setRoleName("单位");
//            return ajax.put("roles", roleService.selectPlatformRoleList(role));
//        }else if (loginAncestorssplit.length == 2){
//                // 单位级用户
//                SysRole role = new SysRole();
//                role.setRoleName("普通");
//                return ajax.put("roles", roleService.selectPlatformRoleList(role));
//        }else {
//            return ajax.put("roles", roleService.selectRoleAll());
//        }

//        System.out.println("@@@@@@@@@@@@@@@@");

        if (StringUtils.isNotNull(userId))
        {
            userService.checkUserDataScope(userId);
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        return ajax;
    }

    /**
     * 新增用户
     */
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user)
    {
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));

        Long deptId = user.getDeptId();
//        System.out.println("@@@@@@@@@@@@@@@@@@@");
//        System.out.println(deptId);
        // 获取 权限
        SysDept sysDept = deptService.selectDeptById(deptId);
        String ancestors = sysDept.getAncestors();
        String[] ancestorssplit = ancestors.split(",");

        Long unidId = -1L;
        if (ancestorssplit.length >= 3) {
            unidId = Long.valueOf(ancestorssplit[2]);
        }else {
            unidId = user.getDeptId();
        }
//        if (ancestorssplit.length == 1) {
//            unidId = user.getDeptId();
//        } else if (ancestorssplit.length == 2) {
//            unidId = Long.valueOf(ancestorssplit[1]);
//        }else if (ancestorssplit.length >= 3) {
//            unidId = Long.valueOf(ancestorssplit[2]);
//        }
        user.setUnitId(unidId);

        userService.insertUser(user);
        return toAjax(1);
    }

    /**
     * 修改用户
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user)
    {
        String oldUserName = userService.getUserNameByUserId(user.getUserId());
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，用户名已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getUsername());

        if(oldUserName != null && user.getUserName()!= null){
            userService.updateUser(user);
            userService.batchUpdateAllTablesByUserName(user.getUserName(), oldUserName);
            return toAjax(1);
        }else {
            return toAjax(0);
        }
    }

    /**
     * 删除用户
     */
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        if (ArrayUtils.contains(userIds, getUserId()))
        {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 用户授权角色
     */
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds)
    {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        userService.insertUserAuth(userId, roleIds);
        return success();
    }

    /**
     * 获取部门树列表
     */
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDept dept)
    {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser loginUserInfo = loginUser.getUser();
        // 获取 权限
        Long loginDeptId = loginUserInfo.getDeptId();
        SysDept loginSysDept = deptService.selectDeptById(loginDeptId);
        String loginAncestors = loginSysDept.getAncestors();
        String[] loginAncestorssplit = loginAncestors.split(",");
        String roleKey = roleService.selectRoleKeyByUserId(loginUserInfo.getUserId());
        if (loginUserInfo.isAdmin()){
            return success(deptService.selectDeptTreeList(dept));
        } else {
            if (roleKey.contains("platAdmin")){
                return success(deptService.selectDeptTreeList(dept));
            }else if (roleKey.contains("deptAdmin")){
                dept.setDeptId(loginUserInfo.getDeptId());
                return success(deptService.selectDeptTreeByParentId(dept));
            }else {
                // 普通用户
                dept.setDeptId(Long.valueOf(loginAncestorssplit[2]));
                dept.setParentId(loginUserInfo.getDeptId());
                return success(deptService.selectDeptTreeByParentId(dept));
            }
        }
//
//        if (loginUserInfo.isAdmin()){
//            return success(deptService.selectDeptTreeList(dept));
//        } else {
//            if (roleKey.contains("platAdmin")){
//                return success(deptService.selectDeptTreeList(dept));
//            }else if (roleKey.contains("deptAdmin")){
//                dept.setDeptId(loginUserInfo.getDeptId());
//                return success(deptService.selectDeptTreeByParentId(dept));
//            }else {
//                // 普通用户
//                dept.setDeptId(Long.valueOf(loginAncestorssplit[2]));
//                dept.setParentId(loginUserInfo.getDeptId());
//                return success(deptService.selectDeptTreeByParentId(dept));
//            }
//        }

//        if (loginUserInfo.isAdmin()){
////            dept.setDeptId(loginUserInfo.getDeptId());
//            return success(deptService.selectDeptTreeList(dept));
//        } else if (loginAncestorssplit.length == 1){
//            dept.setAncestors(String.valueOf(loginUserInfo.getDeptId()));
//            return success(deptService.selectDeptTreeList(dept));
//        }else if (loginAncestorssplit.length == 2){
//            // 单位级用户
//            dept.setParentId(loginUserInfo.getDeptId());
//            return success(deptService.selectDeptTreeList(dept));
//        } else {
//            // 普通用户
//            dept.setDeptId(Long.valueOf(loginAncestorssplit[2]));
//            dept.setParentId(loginUserInfo.getDeptId());
//            return success(deptService.selectDeptTreeList(dept));
//        }
    }
}
