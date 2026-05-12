package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.List;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysRoleService;
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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysDeptService;

/**
 * 部门信息
 * 
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController
{
    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysRoleService roleService;

    /**
     * 获取部门列表
     */
    @GetMapping("/list")
    public AjaxResult list(SysDept dept)
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

//        if (loginUserInfo.isAdmin()){
//            return success(deptService.selectDeptList(dept));
//        } else if (loginAncestorssplit.length == 1){
////            dept.setParentId(loginUserInfo.getDeptId());
//            return success(deptService.selectDeptList(dept));
//        }else if (loginAncestorssplit.length == 2){
//            // 单位级用户
//            dept.setDeptId(loginUserInfo.getDeptId());
//            return success(deptService.selectDeptListByParentId(dept));
//        } else {
//            // 普通用户
//            dept.setDeptId(Long.valueOf(loginAncestorssplit[2]));
//            dept.setParentId(loginUserInfo.getDeptId());
//            return success(deptService.selectDeptListByParentId(dept));
//        }

//        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//        SysUser user = loginUser.getUser();
//        if (!user.isAdmin()){
//            Long deptId = user.getDeptId();
//            // 获取 权限
//            SysDept sysDept = deptService.selectDeptById(deptId);
//            String ancestors = sysDept.getAncestors();
//            String[] ancestorssplit = ancestors.split(",");
//            Long unidId = -1L;
//            if (ancestorssplit.length == 2) {
//                // 单位级用户
//                unidId = user.getDeptId();
//            } else if (ancestorssplit.length == 3) {
//                // 单位
//                unidId = Long.valueOf(ancestorssplit[2]);
//            }
//            dept.setDeptId(unidId);
//            dept.setParentId(unidId);
//        }
//
//        List<SysDept> depts = deptService.selectDeptList(dept);
//        return success(depts);
    }

    @GetMapping("/getUnitStatistics")
    public AjaxResult getUnitStatistics()
    {
        return success(deptService.getUnitStatistics());
    }


    /**
     * 查询部门列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public AjaxResult excludeChild(@PathVariable(value = "deptId", required = false) Long deptId)
    {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        depts.removeIf(d -> d.getDeptId().intValue() == deptId || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
        return success(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId)
    {
        deptService.checkDeptDataScope(deptId);
        return success(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysDept dept)
    {
        if (!deptService.checkDeptNameUnique(dept))
        {
            return error("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.setCreateBy(getUsername());
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysDept dept)
    {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept))
        {
            return error("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        else if (dept.getParentId().equals(deptId))
        {
            return error("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0)
        {
            return error("该部门包含未停用的子部门！");
        }
        dept.setUpdateBy(getUsername());
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public AjaxResult remove(@PathVariable Long deptId)
    {
        if (deptService.hasChildByDeptId(deptId))
        {
            return warn("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId))
        {
            return warn("部门存在用户,不允许删除");
        }
        deptService.checkDeptDataScope(deptId);
        return toAjax(deptService.deleteDeptById(deptId));
    }
}
