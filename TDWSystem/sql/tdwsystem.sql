/*
 Navicat Premium Data Transfer

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80041
 Source Host           : 127.0.0.1:3306
 Source Schema         : tdwsystem

 Target Server Type    : MySQL
 Target Server Version : 80041
 File Encoding         : 65001

 Date: 28/03/2026 10:50:14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table
-- ----------------------------
INSERT INTO `gen_table` VALUES (11, 'tdw_bids', '', NULL, NULL, 'TdwBids', 'crud', '', 'com.ruoyi.system', 'system', 'bids', NULL, 'ruoyi', '0', '/', NULL, 'admin', '2026-03-27 16:30:59', '', NULL, NULL);
INSERT INTO `gen_table` VALUES (12, 'tdw_contents', '', NULL, NULL, 'TdwContents', 'crud', '', 'com.ruoyi.system', 'system', 'contents', NULL, 'ruoyi', '0', '/', NULL, 'admin', '2026-03-27 16:30:59', '', NULL, NULL);
INSERT INTO `gen_table` VALUES (13, 'tdw_outlines', '', NULL, NULL, 'TdwOutlines', 'crud', '', 'com.ruoyi.system', 'system', 'outlines', NULL, 'ruoyi', '0', '/', NULL, 'admin', '2026-03-27 16:30:59', '', NULL, NULL);

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint(0) DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典类型',
  `sort` int(0) DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 159 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
INSERT INTO `gen_table_column` VALUES (159, 11, 'id', NULL, 'int', 'Long', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (160, 11, 'title', '表示名称', 'varchar(255)', 'String', 'title', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (161, 11, 'template_id', '关联的模板ID', 'int', 'Long', 'templateId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (162, 11, 'status', '状态：1-草稿，2-已完成', 'int', 'Long', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 4, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (163, 11, 'created_time', '创建时间', 'datetime', 'Date', 'createdTime', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'datetime', '', 5, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (164, 11, 'userid', '用户id', 'int', 'Long', 'userid', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (165, 11, 'deptid', '部门id', 'int', 'Long', 'deptid', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (166, 11, 'unitid', '单位id', 'int', 'Long', 'unitid', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (167, 11, 'note', '备注', 'varchar(255)', 'String', 'note', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 9, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (168, 12, 'id', '大纲节点id', 'bigint', 'Long', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (169, 12, 'outline_id', '关联的大纲节点ID,通常挂载在level=3的节点下', 'bigint', 'Long', 'outlineId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (170, 12, 'content', '内容', 'longtext', 'String', 'content', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'editor', '', 3, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (171, 12, 'sort_order', '节点下内容的显示顺序', 'int', 'Long', 'sortOrder', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (172, 13, 'id', '大纲节点id', 'bigint', 'Long', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (173, 13, 'bid_id', '所属标书id', 'bigint', 'Long', 'bidId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (174, 13, 'parent_id', '父节点id', 'bigint(20) unsigned zerofill', 'Long', 'parentId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (175, 13, 'level', '层级（1:章, 2:节, 3:内容标题）', 'int', 'Long', 'level', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (176, 13, 'title', '大纲标题', 'varchar(255)', 'String', 'title', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (177, 13, 'path', '树形路径（关键字段）。格式如：/10/ (章), /10/15/ (节), /10/15/22/ (内容)', 'varchar(255)', 'String', 'path', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-03-27 16:30:59', '', NULL);
INSERT INTO `gen_table_column` VALUES (178, 13, 'sort_num', '同级排序号', 'int', 'Long', 'sortNum', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2026-03-27 16:30:59', '', NULL);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2025-07-09 15:40:10', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2025-07-09 15:40:10', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2025-07-09 15:40:10', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'false', 'Y', 'admin', '2025-07-09 15:40:10', '', NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2025-07-09 15:40:10', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2025-07-09 15:40:10', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO `sys_config` VALUES (7, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2025-07-09 15:40:10', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` VALUES (8, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2025-07-09 15:40:10', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(0) DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '部门名称',
  `order_num` int(0) DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 260 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '数投科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-07-09 15:40:03', 'admin', '2025-07-13 17:39:21');
INSERT INTO `sys_dept` VALUES (212, 100, '0,100', '保定市科学技术局', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:20:15', 'st-admin', '2025-08-28 10:12:58');
INSERT INTO `sys_dept` VALUES (213, 212, '0,100,212', '办公室', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:20:27', '', NULL);
INSERT INTO `sys_dept` VALUES (214, 212, '0,100,212', '人事科', 2, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:20:57', '', NULL);
INSERT INTO `sys_dept` VALUES (215, 212, '0,100,212', '机关党委', 3, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:21:06', '', NULL);
INSERT INTO `sys_dept` VALUES (216, 212, '0,100,212', '社会发展科技科', 4, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:21:16', '', NULL);
INSERT INTO `sys_dept` VALUES (217, 212, '0,100,212', '农村科技科', 5, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:21:22', '', NULL);
INSERT INTO `sys_dept` VALUES (218, 212, '0,100,212', '科技奖励与成果转化科', 6, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:21:28', '', NULL);
INSERT INTO `sys_dept` VALUES (219, 212, '0,100,212', '外国专家管理和国际合作科', 7, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:21:35', '', NULL);
INSERT INTO `sys_dept` VALUES (220, 212, '0,100,212', '区域创新科', 8, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:21:41', '', NULL);
INSERT INTO `sys_dept` VALUES (221, 212, '0,100,212', '高新技术科', 9, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:21:47', '', NULL);
INSERT INTO `sys_dept` VALUES (222, 212, '0,100,212', '科技平台与基础研究科', 10, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:21:52', '', NULL);
INSERT INTO `sys_dept` VALUES (223, 212, '0,100,212', '资源配置管理和监督评估科', 11, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:22:02', '', NULL);
INSERT INTO `sys_dept` VALUES (224, 212, '0,100,212', '综合规划和政策法规科', 12, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:22:07', '', NULL);
INSERT INTO `sys_dept` VALUES (225, 100, '0,100', '保定市财政局', 2, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:24:30', 'st-admin', '2025-08-27 19:55:39');
INSERT INTO `sys_dept` VALUES (226, 225, '0,100,225', '办公室', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:24:39', '', NULL);
INSERT INTO `sys_dept` VALUES (227, 225, '0,100,225', '人事处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:24:53', '', NULL);
INSERT INTO `sys_dept` VALUES (228, 225, '0,100,225', '政策研究室', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:24:59', '', NULL);
INSERT INTO `sys_dept` VALUES (229, 225, '0,100,225', '预算处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:04', '', NULL);
INSERT INTO `sys_dept` VALUES (230, 225, '0,100,225', '政府债务管理处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:12', '', NULL);
INSERT INTO `sys_dept` VALUES (231, 225, '0,100,225', '国库处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:17', '', NULL);
INSERT INTO `sys_dept` VALUES (232, 225, '0,100,225', '稽核处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:22', '', NULL);
INSERT INTO `sys_dept` VALUES (233, 225, '0,100,225', '监督评价处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:26', '', NULL);
INSERT INTO `sys_dept` VALUES (234, 225, '0,100,225', '治税管理处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:30', 'czj-admin', '2025-08-25 22:48:32');
INSERT INTO `sys_dept` VALUES (235, 225, '0,100,225', '计划处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:35', '', NULL);
INSERT INTO `sys_dept` VALUES (236, 225, '0,100,225', '产权处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:39', '', NULL);
INSERT INTO `sys_dept` VALUES (237, 225, '0,100,225', '行政政法处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:44', '', NULL);
INSERT INTO `sys_dept` VALUES (238, 225, '0,100,225', '社会保障处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:25:56', '', NULL);
INSERT INTO `sys_dept` VALUES (239, 225, '0,100,225', '农业农村处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:26:00', '', NULL);
INSERT INTO `sys_dept` VALUES (240, 225, '0,100,225', '经济建设处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:26:04', '', NULL);
INSERT INTO `sys_dept` VALUES (241, 225, '0,100,225', '资源和环境保护处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:26:14', '', NULL);
INSERT INTO `sys_dept` VALUES (242, 225, '0,100,225', '综合法规处', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-19 11:26:18', 'admin', '2025-09-11 16:10:04');
INSERT INTO `sys_dept` VALUES (247, 100, '0,100', '保定市政府', 3, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-21 15:21:33', '', NULL);
INSERT INTO `sys_dept` VALUES (248, 247, '0,100,247', '市委办公室', 1, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-21 15:21:41', 'admin', '2025-09-12 11:19:29');
INSERT INTO `sys_dept` VALUES (255, 100, '0,100', '保定市社会保障局', 5, NULL, NULL, NULL, '0', '0', 'admin', '2025-08-26 13:42:33', '', NULL);
INSERT INTO `sys_dept` VALUES (259, 100, '0,100', '财政局', 5, NULL, NULL, NULL, '0', '0', 'admin', '2025-09-11 17:41:41', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int(0) DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '停用状态');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2025-07-09 15:40:10', '', NULL, '登录状态列表');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `job_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时任务调度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
  `job_log_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '异常信息',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时任务调度日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '用户账号',
  `unit_id` bigint(0) DEFAULT NULL COMMENT '单位ID',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '提示消息',
  `login_time` datetime(0) DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status`) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`login_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2019 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统访问记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (1944, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-10 13:53:31');
INSERT INTO `sys_logininfor` VALUES (1945, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-10 17:42:53');
INSERT INTO `sys_logininfor` VALUES (1946, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-10 18:46:32');
INSERT INTO `sys_logininfor` VALUES (1947, 'st-admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 14:51:23');
INSERT INTO `sys_logininfor` VALUES (1948, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 14:51:30');
INSERT INTO `sys_logininfor` VALUES (1949, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 16:05:18');
INSERT INTO `sys_logininfor` VALUES (1950, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 16:25:39');
INSERT INTO `sys_logininfor` VALUES (1951, 'czj-admin', 100, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 16:25:44');
INSERT INTO `sys_logininfor` VALUES (1952, 'czj-admin', 100, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 16:27:14');
INSERT INTO `sys_logininfor` VALUES (1953, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 16:27:17');
INSERT INTO `sys_logininfor` VALUES (1954, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 16:28:29');
INSERT INTO `sys_logininfor` VALUES (1955, 'czj-admin', 100, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 16:28:32');
INSERT INTO `sys_logininfor` VALUES (1956, 'czj-admin', 100, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 16:36:36');
INSERT INTO `sys_logininfor` VALUES (1957, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 16:36:41');
INSERT INTO `sys_logininfor` VALUES (1958, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 16:54:49');
INSERT INTO `sys_logininfor` VALUES (1959, 'czj-admin', 100, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 16:54:52');
INSERT INTO `sys_logininfor` VALUES (1960, 'czj-admin', 100, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 16:55:11');
INSERT INTO `sys_logininfor` VALUES (1961, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 16:55:15');
INSERT INTO `sys_logininfor` VALUES (1962, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 16:55:25');
INSERT INTO `sys_logininfor` VALUES (1963, 'czj-admin', 100, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 16:55:29');
INSERT INTO `sys_logininfor` VALUES (1964, 'czj-admin', 100, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 17:03:08');
INSERT INTO `sys_logininfor` VALUES (1965, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 17:03:12');
INSERT INTO `sys_logininfor` VALUES (1966, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 17:17:54');
INSERT INTO `sys_logininfor` VALUES (1967, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 17:17:58');
INSERT INTO `sys_logininfor` VALUES (1968, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 17:18:27');
INSERT INTO `sys_logininfor` VALUES (1969, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 17:18:30');
INSERT INTO `sys_logininfor` VALUES (1970, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 17:19:11');
INSERT INTO `sys_logininfor` VALUES (1971, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 17:19:15');
INSERT INTO `sys_logininfor` VALUES (1972, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 17:19:27');
INSERT INTO `sys_logininfor` VALUES (1973, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 17:19:32');
INSERT INTO `sys_logininfor` VALUES (1974, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 17:20:05');
INSERT INTO `sys_logininfor` VALUES (1975, 'czj-bgs1', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-09-11 17:20:09');
INSERT INTO `sys_logininfor` VALUES (1976, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 17:20:20');
INSERT INTO `sys_logininfor` VALUES (1977, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-11 17:23:07');
INSERT INTO `sys_logininfor` VALUES (1978, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 17:23:09');
INSERT INTO `sys_logininfor` VALUES (1979, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 19:31:11');
INSERT INTO `sys_logininfor` VALUES (1980, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 20:04:25');
INSERT INTO `sys_logininfor` VALUES (1981, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 21:09:02');
INSERT INTO `sys_logininfor` VALUES (1982, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-11 21:45:37');
INSERT INTO `sys_logininfor` VALUES (1983, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-12 10:24:58');
INSERT INTO `sys_logininfor` VALUES (1984, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-12 11:12:11');
INSERT INTO `sys_logininfor` VALUES (1985, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-12 11:12:39');
INSERT INTO `sys_logininfor` VALUES (1986, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-12 20:30:11');
INSERT INTO `sys_logininfor` VALUES (1987, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-12 22:27:48');
INSERT INTO `sys_logininfor` VALUES (1988, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-12 22:28:07');
INSERT INTO `sys_logininfor` VALUES (1989, 'st-admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-09-12 22:28:14');
INSERT INTO `sys_logininfor` VALUES (1990, 'czj-admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-09-12 22:28:18');
INSERT INTO `sys_logininfor` VALUES (1991, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-12 22:28:21');
INSERT INTO `sys_logininfor` VALUES (1992, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-12 22:29:06');
INSERT INTO `sys_logininfor` VALUES (1993, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-12 22:29:09');
INSERT INTO `sys_logininfor` VALUES (1994, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-12 22:29:37');
INSERT INTO `sys_logininfor` VALUES (1995, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-12 22:29:41');
INSERT INTO `sys_logininfor` VALUES (1996, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-13 08:37:02');
INSERT INTO `sys_logininfor` VALUES (1997, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 15:14:46');
INSERT INTO `sys_logininfor` VALUES (1998, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-14 15:28:09');
INSERT INTO `sys_logininfor` VALUES (1999, 'st-admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-09-14 15:28:15');
INSERT INTO `sys_logininfor` VALUES (2000, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 15:28:52');
INSERT INTO `sys_logininfor` VALUES (2001, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-14 15:29:02');
INSERT INTO `sys_logininfor` VALUES (2002, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 15:29:09');
INSERT INTO `sys_logininfor` VALUES (2003, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 16:14:46');
INSERT INTO `sys_logininfor` VALUES (2004, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-14 16:17:32');
INSERT INTO `sys_logininfor` VALUES (2005, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 16:17:41');
INSERT INTO `sys_logininfor` VALUES (2006, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 17:32:17');
INSERT INTO `sys_logininfor` VALUES (2007, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 20:29:56');
INSERT INTO `sys_logininfor` VALUES (2008, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-14 20:37:18');
INSERT INTO `sys_logininfor` VALUES (2009, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 20:37:22');
INSERT INTO `sys_logininfor` VALUES (2010, 'czj-admin', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-14 20:37:34');
INSERT INTO `sys_logininfor` VALUES (2011, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 20:37:37');
INSERT INTO `sys_logininfor` VALUES (2012, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 20:43:24');
INSERT INTO `sys_logininfor` VALUES (2013, 'czj-bgs1', 225, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-09-14 21:51:56');
INSERT INTO `sys_logininfor` VALUES (2014, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-14 21:52:01');
INSERT INTO `sys_logininfor` VALUES (2015, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-16 11:48:01');
INSERT INTO `sys_logininfor` VALUES (2016, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-16 12:52:16');
INSERT INTO `sys_logininfor` VALUES (2017, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-09-16 17:19:34');
INSERT INTO `sys_logininfor` VALUES (2018, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-18 08:27:32');
INSERT INTO `sys_logininfor` VALUES (2019, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-03-27 15:02:48');
INSERT INTO `sys_logininfor` VALUES (2020, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-03-27 15:03:12');
INSERT INTO `sys_logininfor` VALUES (2021, 'admin', -1, '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-03-27 16:30:43');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(0) DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int(0) DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '路由名称',
  `is_frame` int(0) DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int(0) DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2115 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 12, 'system', NULL, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2025-07-09 15:40:06', 'admin', '2025-09-10 13:54:01', '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 11, 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2025-07-09 15:40:06', 'admin', '2025-09-10 13:53:54', '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 11, 'tool', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2025-07-09 15:40:06', 'admin', '2025-09-10 13:53:58', '系统工具目录');
INSERT INTO `sys_menu` VALUES (100, '成员管理', 0, 9, 'system/user', 'system/user/index', '', '', 1, 0, 'M', '0', '0', 'system:user:list', 'menu-member', 'admin', '2025-07-09 15:40:06', 'admin', '2025-08-26 22:12:02', '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2025-07-09 15:40:06', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2025-07-09 15:40:06', 'admin', '2025-08-24 09:16:33', '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, '单位管理', 0, 10, 'system/dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'menu-dept', 'admin', '2025-07-09 15:40:06', 'admin', '2025-08-27 19:31:09', '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2025-07-09 15:40:06', 'admin', '2025-08-24 09:16:21', '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2025-07-09 15:40:06', 'admin', '2025-08-24 09:16:13', '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2025-07-09 15:40:06', 'admin', '2025-08-24 09:16:40', '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2025-07-09 15:40:06', 'admin', '2025-08-24 09:15:46', '通知公告菜单');
INSERT INTO `sys_menu` VALUES (108, '日志管理', 1, 9, 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2025-07-09 15:40:06', 'admin', '2025-08-24 09:16:47', '日志管理菜单');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2025-07-09 15:40:06', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2025-07-09 15:40:06', 'admin', '2025-08-24 09:17:08', '定时任务菜单');
INSERT INTO `sys_menu` VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2025-07-09 15:40:06', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', '', 1, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2025-07-09 15:40:06', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2025-07-09 15:40:06', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2025-07-09 15:40:06', '', NULL, '缓存列表菜单');
INSERT INTO `sys_menu` VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2025-07-09 15:40:06', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2025-07-09 15:40:06', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2025-07-09 15:40:06', 'admin', '2025-10-18 08:30:37', '系统接口菜单');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2025-07-09 15:40:06', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2025-07-09 15:40:06', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` VALUES (1000, '用户查询', 100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1001, '用户新增', 100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1002, '用户修改', 100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1003, '用户删除', 100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1004, '用户导出', 100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1005, '用户导入', 100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1006, '重置密码', 100, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1007, '角色查询', 101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1008, '角色新增', 101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1009, '角色修改', 101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1010, '角色删除', 101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1011, '角色导出', 101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1012, '菜单查询', 102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1013, '菜单新增', 102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1014, '菜单修改', 102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1015, '菜单删除', 102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1016, '部门查询', 103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1017, '部门新增', 103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1018, '部门修改', 103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1019, '部门删除', 103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1020, '岗位查询', 104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1021, '岗位新增', 104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1022, '岗位修改', 104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1023, '岗位删除', 104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1024, '岗位导出', 104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1025, '字典查询', 105, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1026, '字典新增', 105, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1027, '字典修改', 105, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1028, '字典删除', 105, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1029, '字典导出', 105, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1030, '参数查询', 106, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1031, '参数新增', 106, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1032, '参数修改', 106, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1033, '参数删除', 106, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1034, '参数导出', 106, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1035, '公告查询', 107, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1036, '公告新增', 107, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1037, '公告修改', 107, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1038, '公告删除', 107, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1039, '操作查询', 500, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1040, '操作删除', 500, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1041, '日志导出', 500, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1042, '登录查询', 501, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1043, '登录删除', 501, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1044, '日志导出', 501, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1046, '在线查询', 109, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1047, '批量强退', 109, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1048, '单条强退', 109, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1049, '任务查询', 110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1050, '任务新增', 110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1051, '任务修改', 110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1052, '任务删除', 110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1053, '状态修改', 110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1054, '任务导出', 110, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1055, '生成查询', 116, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1056, '生成修改', 116, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1057, '生成删除', 116, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 116, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 116, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 116, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2025-07-09 15:40:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2004, '智能写作', 0, 2, 'member/writing', 'member/writing/index', NULL, '', 1, 0, 'M', '0', '0', '', 'icon_menu_writing', 'admin', '2025-07-14 09:17:19', 'admin', '2025-08-26 22:17:59', '');
INSERT INTO `sys_menu` VALUES (2005, '知识文库-用户端', 0, 7, 'member/knowledge', 'member/knowledge/index', NULL, '', 1, 0, 'M', '0', '0', '', 'icon_menu_knowledge', 'admin', '2025-07-15 15:11:55', 'admin', '2025-08-26 22:12:34', '');
INSERT INTO `sys_menu` VALUES (2006, '优化工具', 0, 5, 'member/optimize', 'member/optimize/index', NULL, '', 1, 0, 'M', '0', '0', '', 'icon_menu_optimize', 'admin', '2025-07-15 21:22:13', 'admin', '2025-08-26 22:12:43', '');
INSERT INTO `sys_menu` VALUES (2007, '以稿写稿', 0, 3, 'member/rewrite', 'member/rewrite/index', NULL, '', 1, 0, 'M', '0', '0', '', 'icon_menu_rewrite', 'admin', '2025-07-15 22:43:32', 'admin', '2025-08-26 22:12:52', '');
INSERT INTO `sys_menu` VALUES (2019, '我的稿件', 0, 6, 'member/document', 'member/speechdrafts/index', NULL, '', 1, 0, 'M', '0', '0', '', 'icon_menu_document', 'admin', '2025-07-19 22:07:11', 'admin', '2025-08-26 22:12:38', '');
INSERT INTO `sys_menu` VALUES (2020, '知识文库-管理端', 0, 7, 'knowledge/index', 'system/knowledge/index', NULL, '', 1, 0, 'C', '0', '0', '', 'icon_menu_knowledge', 'admin', '2025-07-22 21:41:15', 'admin', '2025-08-26 22:12:30', '');
INSERT INTO `sys_menu` VALUES (2021, '平台文库', 2020, 1, 'knowledge/index', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:knowledge', '#', 'admin', '2025-07-22 23:12:36', 'admin', '2025-08-26 17:40:22', '');
INSERT INTO `sys_menu` VALUES (2022, '单位文库', 2020, 2, 'knowledge/index', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:knowledge', '#', 'admin', '2025-07-22 23:12:56', 'admin', '2025-08-26 17:40:30', '');
INSERT INTO `sys_menu` VALUES (2023, '成员文库', 2020, 3, 'knowledge/index', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:knowledge', '#', 'admin', '2025-07-22 23:13:15', 'admin', '2025-08-26 17:40:39', '');
INSERT INTO `sys_menu` VALUES (2029, '录音智记', 0, 4, 'member/record', 'member/record/index', NULL, '', 1, 0, 'M', '0', '0', '', 'icon_menu_record', 'admin', '2025-07-30 14:56:34', 'admin', '2025-08-26 22:12:47', '');
INSERT INTO `sys_menu` VALUES (2030, '平台文库', 2005, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:knowledge', '#', 'admin', '2025-08-23 17:01:53', 'admin', '2025-08-26 18:35:32', '');
INSERT INTO `sys_menu` VALUES (2034, '单位文库', 2005, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:knowledge', '#', 'admin', '2025-08-23 17:05:06', 'admin', '2025-08-26 18:35:37', '');
INSERT INTO `sys_menu` VALUES (2040, '个人文库', 2005, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:knowledge', '#', 'admin', '2025-08-23 17:09:58', 'admin', '2025-08-26 18:35:52', '');
INSERT INTO `sys_menu` VALUES (2041, '查看', 2040, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:material:view', '#', 'admin', '2025-08-23 17:12:14', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2042, '下载', 2040, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:material:download', '#', 'admin', '2025-08-23 17:15:54', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2043, '删除', 2040, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:material:delete', '#', 'admin', '2025-08-23 17:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2044, '上传', 2040, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:material:upload', '#', 'admin', '2025-08-23 17:16:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2048, '素材', 2021, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:knowledge:material', '#', 'admin', '2025-08-26 16:37:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2049, '上传', 2048, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:material:uploadFile', '#', 'admin', '2025-08-26 16:38:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2050, '查看', 2048, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:material:view', '#', 'admin', '2025-08-26 16:39:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2051, '下载', 2048, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:material:download', '#', 'admin', '2025-08-26 16:39:27', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2052, '删除', 2048, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:material:delete', '#', 'admin', '2025-08-26 16:39:45', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2053, '模板', 2021, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:knowledge:template', '#', 'admin', '2025-08-26 16:41:48', 'admin', '2025-08-26 16:42:13', '');
INSERT INTO `sys_menu` VALUES (2054, '上传', 2053, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:template:uploadFile', '#', 'admin', '2025-08-26 16:46:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2055, '查看', 2053, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:template:view', '#', 'admin', '2025-08-26 16:46:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2056, '下载', 2053, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:template:download', '#', 'admin', '2025-08-26 16:49:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2057, '删除', 2053, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'platform:template:delete', '#', 'admin', '2025-08-26 16:49:27', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2058, '素材', 2022, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:knowledge:material', '#', 'admin', '2025-08-26 17:24:27', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2059, '模板', 2022, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:knowledge:template', '#', 'admin', '2025-08-26 17:24:46', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2060, '上传', 2058, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:material:uploadFile', '#', 'admin', '2025-08-26 17:25:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2061, '查看', 2058, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:material:view', '#', 'admin', '2025-08-26 17:25:59', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2062, '下载', 2058, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:material:download', '#', 'admin', '2025-08-26 17:26:24', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2063, '删除', 2058, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:material:delete', '#', 'admin', '2025-08-26 17:26:46', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2064, '上传', 2059, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:template:uploadFile', '#', 'admin', '2025-08-26 17:27:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2065, '查看', 2059, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:template:view', '#', 'admin', '2025-08-26 17:27:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2066, '下载', 2059, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:template:download', '#', 'admin', '2025-08-26 17:29:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2067, '删除', 2059, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'dept:template:delete', '#', 'admin', '2025-08-26 17:29:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2068, '素材', 2023, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:knowledge:material', '#', 'admin', '2025-08-26 17:30:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2069, '上传', 2068, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:material:uploadFile', '#', 'admin', '2025-08-26 17:30:59', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2070, '查看', 2068, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:material:view', '#', 'admin', '2025-08-26 17:31:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2071, '下载', 2068, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:material:download', '#', 'admin', '2025-08-26 17:31:38', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2072, '删除', 2068, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:material:delete', '#', 'admin', '2025-08-26 17:31:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2073, '模板', 2023, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:knowledge:template', '#', 'admin', '2025-08-26 17:32:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2074, '上传', 2073, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:template:uploadFile', '#', 'admin', '2025-08-26 17:32:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2075, '查看', 2073, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:template:view', '#', 'admin', '2025-08-26 17:32:58', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2076, '下载', 2073, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:template:download', '#', 'admin', '2025-08-26 17:33:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2077, '删除', 2073, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'member:template:delete', '#', 'admin', '2025-08-26 17:33:38', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2078, '素材', 2030, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:material', '#', 'admin', '2025-08-26 17:42:11', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2079, '上传', 2078, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:material:uploadFile', '#', 'admin', '2025-08-26 17:42:37', 'admin', '2025-08-26 17:43:10', '');
INSERT INTO `sys_menu` VALUES (2080, '查看', 2078, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:material:view', '#', 'admin', '2025-08-26 17:43:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2081, '下载', 2078, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:material:download', '#', 'admin', '2025-08-26 17:43:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2082, '删除', 2078, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:material:delete', '#', 'admin', '2025-08-26 17:44:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2083, '模板', 2030, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:template', '#', 'admin', '2025-08-26 17:44:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2084, '上传', 2083, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:template:uploadFile', '#', 'admin', '2025-08-26 18:31:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2085, '查看', 2083, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:template:view', '#', 'admin', '2025-08-26 18:31:27', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2086, '下载', 2083, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:template:download', '#', 'admin', '2025-08-26 18:31:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2087, '删除', 2083, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:platform:template:delete', '#', 'admin', '2025-08-26 18:32:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2088, '素材', 2034, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:material', '#', 'admin', '2025-08-26 18:39:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2089, '模板', 2034, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:template', '#', 'admin', '2025-08-26 18:40:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2090, '上传', 2088, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:material:uploadFile', '#', 'admin', '2025-08-26 18:40:43', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2091, '查看', 2088, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:material:view', '#', 'admin', '2025-08-26 18:40:58', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2092, '下载', 2088, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:material:download', '#', 'admin', '2025-08-26 18:41:14', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2093, '删除', 2088, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:material:delete', '#', 'admin', '2025-08-26 18:41:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2094, '上传', 2089, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:template:uploadFile', '#', 'admin', '2025-08-26 18:42:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2095, '查看', 2089, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:template:view', '#', 'admin', '2025-08-26 18:42:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2096, '下载', 2089, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:template:download', '#', 'admin', '2025-08-26 18:42:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2097, '删除', 2089, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:dept:template:delete', '#', 'admin', '2025-08-26 18:42:51', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2098, '素材', 2040, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:material', '#', 'admin', '2025-08-26 19:05:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2099, '模板', 2040, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:template', '#', 'admin', '2025-08-26 19:05:38', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2100, '上传', 2098, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:material:uploadFile', '#', 'admin', '2025-08-26 19:06:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2101, '查看', 2098, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:material:view', '#', 'admin', '2025-08-26 19:06:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2102, '下载', 2098, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:material:download', '#', 'admin', '2025-08-26 19:06:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2103, '删除', 2098, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:material:delete', '#', 'admin', '2025-08-26 19:06:41', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2104, '上传', 2099, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:template:uploadFile', '#', 'admin', '2025-08-26 19:07:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2105, '查看', 2099, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:template:view', '#', 'admin', '2025-08-26 19:07:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2106, '下载', 2099, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:template:download', '#', 'admin', '2025-08-26 19:07:43', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2107, '删除', 2099, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'user:member:template:delete', '#', 'admin', '2025-08-26 19:07:58', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2108, '角色管理', 0, 8, 'system/role/index', 'system/role/index', NULL, '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2025-08-26 20:15:49', 'admin', '2025-08-26 22:12:24', '');
INSERT INTO `sys_menu` VALUES (2109, '角色查询', 2108, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2025-08-26 20:16:24', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2110, '角色新增', 2108, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2025-08-26 20:16:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2111, '角色修改', 2108, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2025-08-26 20:17:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2112, '角色删除', 2108, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2025-08-26 20:17:24', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2113, '角色导出', 2108, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2025-08-26 20:17:47', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2114, '驾驶舱', 0, 1, 'index', 'index', NULL, '', 1, 0, 'C', '0', '0', '', 'menu-dashboard', 'admin', '2025-08-26 20:43:28', 'admin', '2025-08-26 22:18:05', '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2025-07-09 15:40:13', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2025-07-09 15:40:13', '', NULL, '管理员');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '模块标题',
  `business_type` int(0) DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '请求方式',
  `operator_type` int(0) DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '部门名称',
  `unit_id` bigint(0) DEFAULT NULL COMMENT '单位ID',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '返回参数',
  `status` int(0) DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime(0) DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint(0) DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type`) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status`) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6187 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (5740, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-07-09 15:40:06\",\"icon\":\"monitor\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2,\"menuName\":\"系统监控\",\"menuType\":\"M\",\"orderNum\":11,\"params\":{},\"parentId\":0,\"path\":\"monitor\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"}', '', 0, NULL, '2025-09-10 13:53:54', 53);
INSERT INTO `sys_oper_log` VALUES (5741, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-07-09 15:40:06\",\"icon\":\"tool\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":3,\"menuName\":\"系统工具\",\"menuType\":\"M\",\"orderNum\":11,\"params\":{},\"parentId\":0,\"path\":\"tool\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"}', '', 0, NULL, '2025-09-10 13:53:58', 65);
INSERT INTO `sys_oper_log` VALUES (5742, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-07-09 15:40:06\",\"icon\":\"system\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":1,\"menuName\":\"系统管理\",\"menuType\":\"M\",\"orderNum\":12,\"params\":{},\"parentId\":0,\"path\":\"system\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:01', 53);
INSERT INTO `sys_oper_log` VALUES (5743, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:15', 22);
INSERT INTO `sys_oper_log` VALUES (5744, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/129', '127.0.0.1', '内网IP', '[129]', '', 0, NULL, '2025-09-10 13:54:40', 68);
INSERT INTO `sys_oper_log` VALUES (5745, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:40', 15);
INSERT INTO `sys_oper_log` VALUES (5746, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/128', '127.0.0.1', '内网IP', '[128]', '', 0, NULL, '2025-09-10 13:54:42', 66);
INSERT INTO `sys_oper_log` VALUES (5747, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:42', 10);
INSERT INTO `sys_oper_log` VALUES (5748, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/124', '127.0.0.1', '内网IP', '[124]', '', 0, NULL, '2025-09-10 13:54:44', 49);
INSERT INTO `sys_oper_log` VALUES (5749, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:44', 8);
INSERT INTO `sys_oper_log` VALUES (5750, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/123', '127.0.0.1', '内网IP', '[123]', '', 0, NULL, '2025-09-10 13:54:46', 50);
INSERT INTO `sys_oper_log` VALUES (5751, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:46', 7);
INSERT INTO `sys_oper_log` VALUES (5752, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/122', '127.0.0.1', '内网IP', '[122]', '', 0, NULL, '2025-09-10 13:54:48', 183);
INSERT INTO `sys_oper_log` VALUES (5753, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:48', 9);
INSERT INTO `sys_oper_log` VALUES (5754, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/121', '127.0.0.1', '内网IP', '[121]', '', 0, NULL, '2025-09-10 13:54:50', 50);
INSERT INTO `sys_oper_log` VALUES (5755, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:50', 7);
INSERT INTO `sys_oper_log` VALUES (5756, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/120', '127.0.0.1', '内网IP', '[120]', '', 0, NULL, '2025-09-10 13:54:51', 65);
INSERT INTO `sys_oper_log` VALUES (5757, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:52', 10);
INSERT INTO `sys_oper_log` VALUES (5758, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/119', '127.0.0.1', '内网IP', '[119]', '', 0, NULL, '2025-09-10 13:54:53', 50);
INSERT INTO `sys_oper_log` VALUES (5759, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:54', 16);
INSERT INTO `sys_oper_log` VALUES (5760, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/117', '127.0.0.1', '内网IP', '[117]', '', 0, NULL, '2025-09-10 13:54:56', 57);
INSERT INTO `sys_oper_log` VALUES (5761, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:56', 11);
INSERT INTO `sys_oper_log` VALUES (5762, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/116', '127.0.0.1', '内网IP', '[116]', '', 0, NULL, '2025-09-10 13:54:57', 41);
INSERT INTO `sys_oper_log` VALUES (5763, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:57', 10);
INSERT INTO `sys_oper_log` VALUES (5764, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/114', '127.0.0.1', '内网IP', '[114]', '', 0, NULL, '2025-09-10 13:54:59', 48);
INSERT INTO `sys_oper_log` VALUES (5765, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:54:59', 7);
INSERT INTO `sys_oper_log` VALUES (5766, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/113', '127.0.0.1', '内网IP', '[113]', '', 0, NULL, '2025-09-10 13:55:01', 39);
INSERT INTO `sys_oper_log` VALUES (5767, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:55:01', 8);
INSERT INTO `sys_oper_log` VALUES (5768, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/112', '127.0.0.1', '内网IP', '[112]', '', 0, NULL, '2025-09-10 13:55:03', 56);
INSERT INTO `sys_oper_log` VALUES (5769, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:55:03', 6);
INSERT INTO `sys_oper_log` VALUES (5770, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/111', '127.0.0.1', '内网IP', '[111]', '', 0, NULL, '2025-09-10 13:55:05', 49);
INSERT INTO `sys_oper_log` VALUES (5771, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:55:05', 7);
INSERT INTO `sys_oper_log` VALUES (5772, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/109', '127.0.0.1', '内网IP', '[109]', '', 0, NULL, '2025-09-10 13:55:07', 73);
INSERT INTO `sys_oper_log` VALUES (5773, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:55:07', 12);
INSERT INTO `sys_oper_log` VALUES (5774, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/108', '127.0.0.1', '内网IP', '[108]', '', 0, NULL, '2025-09-10 13:55:09', 50);
INSERT INTO `sys_oper_log` VALUES (5775, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:55:09', 7);
INSERT INTO `sys_oper_log` VALUES (5776, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/132', '127.0.0.1', '内网IP', '[132]', '', 0, NULL, '2025-09-10 13:55:11', 39);
INSERT INTO `sys_oper_log` VALUES (5777, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 13:55:11', 5);
INSERT INTO `sys_oper_log` VALUES (5778, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 14:07:14', 56);
INSERT INTO `sys_oper_log` VALUES (5779, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/280', '127.0.0.1', '内网IP', '280', '', 0, NULL, '2025-09-10 14:07:52', 85);
INSERT INTO `sys_oper_log` VALUES (5780, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/272', '127.0.0.1', '内网IP', '272', '', 0, NULL, '2025-09-10 14:07:54', 64);
INSERT INTO `sys_oper_log` VALUES (5781, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/261', '127.0.0.1', '内网IP', '261', '', 0, NULL, '2025-09-10 14:07:57', 2);
INSERT INTO `sys_oper_log` VALUES (5782, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/262', '127.0.0.1', '内网IP', '262', '', 0, NULL, '2025-09-10 14:08:04', 118);
INSERT INTO `sys_oper_log` VALUES (5783, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/261', '127.0.0.1', '内网IP', '261', '', 0, NULL, '2025-09-10 14:08:06', 64);
INSERT INTO `sys_oper_log` VALUES (5784, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/257', '127.0.0.1', '内网IP', '257', '', 0, NULL, '2025-09-10 14:08:09', 62);
INSERT INTO `sys_oper_log` VALUES (5785, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/256', '127.0.0.1', '内网IP', '256', '', 0, NULL, '2025-09-10 14:08:12', 71);
INSERT INTO `sys_oper_log` VALUES (5786, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/251', '127.0.0.1', '内网IP', '251', '', 0, NULL, '2025-09-10 14:08:15', 55);
INSERT INTO `sys_oper_log` VALUES (5787, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757484577419\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 14:09:37', 7);
INSERT INTO `sys_oper_log` VALUES (5788, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"createBy\":\"admin\",\"deptId\":256,\"deptName\":\"测试\",\"orderNum\":5,\"params\":{},\"parentId\":100,\"status\":\"0\"}', '', 0, NULL, '2025-09-10 17:43:10', 76);
INSERT INTO `sys_oper_log` VALUES (5789, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/256', '127.0.0.1', '内网IP', '256', '', 0, NULL, '2025-09-10 17:43:14', 64);
INSERT INTO `sys_oper_log` VALUES (5790, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 17:43:17', 76);
INSERT INTO `sys_oper_log` VALUES (5791, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757497417689\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 17:43:38', 11);
INSERT INTO `sys_oper_log` VALUES (5792, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757501204344\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 18:46:44', 3);
INSERT INTO `sys_oper_log` VALUES (5793, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757501301186\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-10 18:48:21', 2);
INSERT INTO `sys_oper_log` VALUES (5794, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 14:51:45', 49);
INSERT INTO `sys_oper_log` VALUES (5795, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"deptId\":\"100\",\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 14:51:52', 14);
INSERT INTO `sys_oper_log` VALUES (5796, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"createBy\":\"admin\",\"deptId\":257,\"deptName\":\"保定数字城市投资发展集团有限公司\",\"orderNum\":5,\"params\":{},\"parentId\":100,\"status\":\"0\"}', '', 0, NULL, '2025-09-11 14:52:42', 67);
INSERT INTO `sys_oper_log` VALUES (5797, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757573604040\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 14:53:24', 9);
INSERT INTO `sys_oper_log` VALUES (5798, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757573839899\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 14:57:19', 39);
INSERT INTO `sys_oper_log` VALUES (5799, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757573843459\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 14:57:23', 4);
INSERT INTO `sys_oper_log` VALUES (5800, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757573940800\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 14:59:01', 45);
INSERT INTO `sys_oper_log` VALUES (5801, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757573943815\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 14:59:04', 7);
INSERT INTO `sys_oper_log` VALUES (5802, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757573946648\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 14:59:06', 5);
INSERT INTO `sys_oper_log` VALUES (5803, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757574271442\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 15:04:31', 5);
INSERT INTO `sys_oper_log` VALUES (5804, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757574311976\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 15:05:12', 5);
INSERT INTO `sys_oper_log` VALUES (5805, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757574351653\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 15:05:51', 4);
INSERT INTO `sys_oper_log` VALUES (5806, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757577936832\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:05:36', 8);
INSERT INTO `sys_oper_log` VALUES (5807, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757578037112\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:07:17', 6);
INSERT INTO `sys_oper_log` VALUES (5808, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757578038895\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:07:19', 6);
INSERT INTO `sys_oper_log` VALUES (5809, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757578041487\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:07:21', 4);
INSERT INTO `sys_oper_log` VALUES (5810, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757578042173\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:07:22', 8);
INSERT INTO `sys_oper_log` VALUES (5811, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757578043036\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:07:23', 4);
INSERT INTO `sys_oper_log` VALUES (5812, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'admin', '数投科技', -1, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757578050579\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-11 16:07:31', 514);
INSERT INTO `sys_oper_log` VALUES (5813, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757578051122\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:07:31', 15);
INSERT INTO `sys_oper_log` VALUES (5814, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试目录\"}', '', 0, NULL, '2025-09-11 16:07:43', 4);
INSERT INTO `sys_oper_log` VALUES (5815, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'admin', '数投科技', -1, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757578073462\",\"folderName\":\"测试目录\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-11 16:07:54', 306);
INSERT INTO `sys_oper_log` VALUES (5816, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试目录\"}', '', 0, NULL, '2025-09-11 16:07:54', 6);
INSERT INTO `sys_oper_log` VALUES (5817, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757578076927\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:07:57', 8);
INSERT INTO `sys_oper_log` VALUES (5818, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试目录\"}', '', 0, NULL, '2025-09-11 16:07:59', 5);
INSERT INTO `sys_oper_log` VALUES (5819, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757578080841\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:08:01', 7);
INSERT INTO `sys_oper_log` VALUES (5820, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":225,\"deptName\":\"保定市财政局\",\"orderNum\":2,\"params\":{},\"parentId\":100,\"parentName\":\"数投科技\",\"status\":\"1\"}', '', 0, NULL, '2025-09-11 16:08:58', 3);
INSERT INTO `sys_oper_log` VALUES (5821, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":247,\"deptName\":\"保定市政府\",\"orderNum\":3,\"params\":{},\"parentId\":100,\"parentName\":\"数投科技\",\"status\":\"1\"}', '', 0, NULL, '2025-09-11 16:09:32', 2);
INSERT INTO `sys_oper_log` VALUES (5822, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":225,\"deptName\":\"保定市财政局\",\"orderNum\":2,\"params\":{},\"parentId\":100,\"parentName\":\"数投科技\",\"status\":\"1\"}', '', 0, NULL, '2025-09-11 16:09:59', 2);
INSERT INTO `sys_oper_log` VALUES (5823, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,225\",\"children\":[],\"deptId\":242,\"deptName\":\"综合法规处\",\"orderNum\":1,\"params\":{},\"parentId\":225,\"parentName\":\"保定市财政局\",\"status\":\"1\",\"updateBy\":\"admin\"}', '', 0, NULL, '2025-09-11 16:10:03', 56);
INSERT INTO `sys_oper_log` VALUES (5824, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,225\",\"children\":[],\"deptId\":242,\"deptName\":\"综合法规处\",\"orderNum\":1,\"params\":{},\"parentId\":225,\"parentName\":\"保定市财政局\",\"status\":\"0\",\"updateBy\":\"admin\"}', '', 0, NULL, '2025-09-11 16:10:05', 74);
INSERT INTO `sys_oper_log` VALUES (5825, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":247,\"deptName\":\"保定市政府\",\"orderNum\":3,\"params\":{},\"parentId\":100,\"parentName\":\"数投科技\",\"status\":\"1\"}', '', 0, NULL, '2025-09-11 16:10:15', 2);
INSERT INTO `sys_oper_log` VALUES (5826, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":247,\"deptName\":\"保定市政府\",\"orderNum\":3,\"params\":{},\"parentId\":100,\"parentName\":\"数投科技\",\"status\":\"1\"}', '', 0, NULL, '2025-09-11 16:18:21', 2);
INSERT INTO `sys_oper_log` VALUES (5827, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,247\",\"children\":[],\"deptId\":248,\"deptName\":\"市委办公室\",\"orderNum\":1,\"params\":{},\"parentId\":247,\"parentName\":\"保定市政府\",\"status\":\"1\",\"updateBy\":\"admin\"}', '', 0, NULL, '2025-09-11 16:18:29', 69);
INSERT INTO `sys_oper_log` VALUES (5828, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,247\",\"children\":[],\"deptId\":248,\"deptName\":\"市委办公室\",\"orderNum\":1,\"params\":{},\"parentId\":247,\"parentName\":\"保定市政府\",\"status\":\"0\",\"updateBy\":\"admin\"}', '', 0, NULL, '2025-09-11 16:18:32', 187);
INSERT INTO `sys_oper_log` VALUES (5829, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,247\",\"children\":[],\"deptId\":248,\"deptName\":\"市委办公室\",\"orderNum\":1,\"params\":{},\"parentId\":247,\"parentName\":\"保定市政府\",\"status\":\"1\",\"updateBy\":\"admin\"}', '', 0, NULL, '2025-09-11 16:18:38', 63);
INSERT INTO `sys_oper_log` VALUES (5830, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:18:41', 14);
INSERT INTO `sys_oper_log` VALUES (5831, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptId\":225,\"nickName\":\"czj-admin\",\"params\":{},\"phonenumber\":\"15000000000\",\"postIds\":[],\"roleIds\":[100],\"status\":\"0\",\"takeSpace\":null,\"unitId\":100,\"userId\":2,\"userName\":\"czj-admin\"}', '', 0, NULL, '2025-09-11 16:25:24', 143);
INSERT INTO `sys_oper_log` VALUES (5832, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:25:25', 6);
INSERT INTO `sys_oper_log` VALUES (5833, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579149854\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:25:49', 3);
INSERT INTO `sys_oper_log` VALUES (5834, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579152309\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:25:52', 8);
INSERT INTO `sys_oper_log` VALUES (5835, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579153320\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:25:53', 3);
INSERT INTO `sys_oper_log` VALUES (5836, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579154692\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:25:55', 4);
INSERT INTO `sys_oper_log` VALUES (5837, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579160469\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:00', 2);
INSERT INTO `sys_oper_log` VALUES (5838, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579167096\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:07', 1);
INSERT INTO `sys_oper_log` VALUES (5839, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:18', 8);
INSERT INTO `sys_oper_log` VALUES (5840, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579180641\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:20', 4);
INSERT INTO `sys_oper_log` VALUES (5841, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579182826\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:22', 3);
INSERT INTO `sys_oper_log` VALUES (5842, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579185051\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:25', 5);
INSERT INTO `sys_oper_log` VALUES (5843, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579186256\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:26', 3);
INSERT INTO `sys_oper_log` VALUES (5844, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579187027\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:27', 6);
INSERT INTO `sys_oper_log` VALUES (5845, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579187675\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:27', 3);
INSERT INTO `sys_oper_log` VALUES (5846, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579188284\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:28', 3);
INSERT INTO `sys_oper_log` VALUES (5847, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757579211028\",\"frontEnd\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:51', 260);
INSERT INTO `sys_oper_log` VALUES (5848, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579211616\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:51', 3);
INSERT INTO `sys_oper_log` VALUES (5849, '后台模板公共知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/template/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579214043\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:54', 3);
INSERT INTO `sys_oper_log` VALUES (5850, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579215372\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:55', 3);
INSERT INTO `sys_oper_log` VALUES (5851, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579217754\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:57', 3);
INSERT INTO `sys_oper_log` VALUES (5852, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579218361\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:26:58', 7);
INSERT INTO `sys_oper_log` VALUES (5853, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579227625\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:07', 3);
INSERT INTO `sys_oper_log` VALUES (5854, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579228611\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:08', 4);
INSERT INTO `sys_oper_log` VALUES (5855, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579230421\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:10', 3);
INSERT INTO `sys_oper_log` VALUES (5856, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579246011\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:26', 3);
INSERT INTO `sys_oper_log` VALUES (5857, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579257426\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:37', 2);
INSERT INTO `sys_oper_log` VALUES (5858, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579258486\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:38', 3);
INSERT INTO `sys_oper_log` VALUES (5859, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579259266\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:39', 2);
INSERT INTO `sys_oper_log` VALUES (5860, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579259775\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:39', 2);
INSERT INTO `sys_oper_log` VALUES (5861, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579260364\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:40', 4);
INSERT INTO `sys_oper_log` VALUES (5862, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579260962\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:40', 2);
INSERT INTO `sys_oper_log` VALUES (5863, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579262084\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:42', 3);
INSERT INTO `sys_oper_log` VALUES (5864, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579262718\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:27:42', 2);
INSERT INTO `sys_oper_log` VALUES (5865, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579314801\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:28:34', 3);
INSERT INTO `sys_oper_log` VALUES (5866, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579711584\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:35:11', 3);
INSERT INTO `sys_oper_log` VALUES (5867, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579721536\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:35:21', 3);
INSERT INTO `sys_oper_log` VALUES (5868, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579728305\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:35:28', 3);
INSERT INTO `sys_oper_log` VALUES (5869, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579729262\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:35:29', 3);
INSERT INTO `sys_oper_log` VALUES (5870, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579730189\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:35:30', 3);
INSERT INTO `sys_oper_log` VALUES (5871, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579732276\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:35:32', 3);
INSERT INTO `sys_oper_log` VALUES (5872, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579733149\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:35:33', 3);
INSERT INTO `sys_oper_log` VALUES (5873, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579780738\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:36:21', 2);
INSERT INTO `sys_oper_log` VALUES (5874, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579781697\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:36:21', 3);
INSERT INTO `sys_oper_log` VALUES (5875, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579782354\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:36:22', 4);
INSERT INTO `sys_oper_log` VALUES (5876, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:36:28', 5);
INSERT INTO `sys_oper_log` VALUES (5877, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579790166\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:36:30', 8);
INSERT INTO `sys_oper_log` VALUES (5878, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579792231\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:36:32', 4);
INSERT INTO `sys_oper_log` VALUES (5879, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579792986\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:36:33', 3);
INSERT INTO `sys_oper_log` VALUES (5880, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:36:45', 10);
INSERT INTO `sys_oper_log` VALUES (5881, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757579806788\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:36:47', 4);
INSERT INTO `sys_oper_log` VALUES (5882, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580819671\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:53:39', 3);
INSERT INTO `sys_oper_log` VALUES (5883, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580833976\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:53:54', 3);
INSERT INTO `sys_oper_log` VALUES (5884, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'admin', '数投科技', -1, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757580862296\",\"frontEnd\":\"1\"}', '', 0, NULL, '2025-09-11 16:54:22', 303);
INSERT INTO `sys_oper_log` VALUES (5885, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580862621\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:54:22', 5);
INSERT INTO `sys_oper_log` VALUES (5886, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580876807\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:54:36', 2);
INSERT INTO `sys_oper_log` VALUES (5887, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580879502\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:54:39', 4);
INSERT INTO `sys_oper_log` VALUES (5888, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580881176\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:54:41', 1);
INSERT INTO `sys_oper_log` VALUES (5889, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580882452\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:54:42', 4);
INSERT INTO `sys_oper_log` VALUES (5890, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580895847\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:54:55', 3);
INSERT INTO `sys_oper_log` VALUES (5891, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580897272\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:54:57', 4);
INSERT INTO `sys_oper_log` VALUES (5892, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580898788\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:54:58', 2);
INSERT INTO `sys_oper_log` VALUES (5893, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580902361\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:02', 3);
INSERT INTO `sys_oper_log` VALUES (5894, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580902926\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:03', 9);
INSERT INTO `sys_oper_log` VALUES (5895, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580903537\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:03', 2);
INSERT INTO `sys_oper_log` VALUES (5896, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580904739\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:05', 5);
INSERT INTO `sys_oper_log` VALUES (5897, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580905303\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:05', 3);
INSERT INTO `sys_oper_log` VALUES (5898, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580906431\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:06', 4);
INSERT INTO `sys_oper_log` VALUES (5899, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580907077\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:07', 3);
INSERT INTO `sys_oper_log` VALUES (5900, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580918570\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:18', 4);
INSERT INTO `sys_oper_log` VALUES (5901, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580919318\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:19', 3);
INSERT INTO `sys_oper_log` VALUES (5902, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580920330\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:20', 2);
INSERT INTO `sys_oper_log` VALUES (5903, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580920902\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:20', 3);
INSERT INTO `sys_oper_log` VALUES (5904, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580921558\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:21', 3);
INSERT INTO `sys_oper_log` VALUES (5905, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580922136\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:22', 1);
INSERT INTO `sys_oper_log` VALUES (5906, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580932187\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:32', 3);
INSERT INTO `sys_oper_log` VALUES (5907, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580933519\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:33', 4);
INSERT INTO `sys_oper_log` VALUES (5908, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580934160\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:55:34', 2);
INSERT INTO `sys_oper_log` VALUES (5909, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580965872\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:56:05', 3);
INSERT INTO `sys_oper_log` VALUES (5910, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580967379\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:56:07', 3);
INSERT INTO `sys_oper_log` VALUES (5911, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757580968439\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:56:08', 4);
INSERT INTO `sys_oper_log` VALUES (5912, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:56:11', 6);
INSERT INTO `sys_oper_log` VALUES (5913, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'czj-admin', '保定市财政局', 100, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"czj-admin\",\"deptId\":226,\"nickName\":\"czj-bgs1\",\"params\":{},\"phonenumber\":\"15000000001\",\"postIds\":[],\"roleIds\":[2],\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"userId\":3,\"userName\":\"czj-bgs1\"}', '', 0, NULL, '2025-09-11 16:56:51', 186);
INSERT INTO `sys_oper_log` VALUES (5914, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:56:51', 6);
INSERT INTO `sys_oper_log` VALUES (5915, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:57:05', 5);
INSERT INTO `sys_oper_log` VALUES (5916, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:57:49', 17);
INSERT INTO `sys_oper_log` VALUES (5917, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757581133016\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:58:53', 3);
INSERT INTO `sys_oper_log` VALUES (5918, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757581134029\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:58:54', 2);
INSERT INTO `sys_oper_log` VALUES (5919, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757581138641\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:58:58', 5);
INSERT INTO `sys_oper_log` VALUES (5920, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 16:59:25', 4);
INSERT INTO `sys_oper_log` VALUES (5921, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 100, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:02:05', 9);
INSERT INTO `sys_oper_log` VALUES (5922, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:03:14', 5);
INSERT INTO `sys_oper_log` VALUES (5923, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/3', '127.0.0.1', '内网IP', '[3]', '', 0, NULL, '2025-09-11 17:08:32', 123);
INSERT INTO `sys_oper_log` VALUES (5924, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:08:32', 103);
INSERT INTO `sys_oper_log` VALUES (5925, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/2', '127.0.0.1', '内网IP', '[2]', '', 0, NULL, '2025-09-11 17:08:36', 120);
INSERT INTO `sys_oper_log` VALUES (5926, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:08:36', 11);
INSERT INTO `sys_oper_log` VALUES (5927, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptId\":225,\"nickName\":\"czj-admin\",\"params\":{},\"phonenumber\":\"15000000000\",\"postIds\":[],\"roleIds\":[100],\"status\":\"0\",\"takeSpace\":null,\"unitId\":100,\"userId\":4,\"userName\":\"czj-admin\"}', '', 0, NULL, '2025-09-11 17:09:02', 138);
INSERT INTO `sys_oper_log` VALUES (5928, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:09:02', 15);
INSERT INTO `sys_oper_log` VALUES (5929, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/user/4', '127.0.0.1', '内网IP', '[4]', '', 0, NULL, '2025-09-11 17:11:32', 85);
INSERT INTO `sys_oper_log` VALUES (5930, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:11:33', 70);
INSERT INTO `sys_oper_log` VALUES (5931, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptId\":225,\"nickName\":\"czj-admin\",\"params\":{},\"phonenumber\":\"15000000000\",\"postIds\":[],\"roleIds\":[100],\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"userId\":5,\"userName\":\"czj-admin\"}', '', 0, NULL, '2025-09-11 17:12:08', 143);
INSERT INTO `sys_oper_log` VALUES (5932, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:12:09', 12);
INSERT INTO `sys_oper_log` VALUES (5933, '角色管理', 3, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-07-14 01:48:10\",\"dataScope\":\"4\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2005,2030,2078,2083,2034,2088,2089,2020,2021,2048,2053,2023,2068,2073,2114,2004,2007,2029,2006,2019,2080,2081,2085,2086,2091,2092,2095,2096,2040,2041,2098,2100,2101,2102,2103,2042,2099,2104,2105,2106,2107,2043,2044,2050,2051,2055,2056,2022,2058,2060,2061,2062,2063,2059,2064,2065,2066,2067,2070,2071,2075,2076,100,1000,1001,1002,1003,1004,1005,1006,103,1016,1017,1018,1019],\"params\":{},\"roleId\":100,\"roleKey\":\"deptAdmin\",\"roleName\":\"单位管理者\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"}', '', 0, NULL, '2025-09-11 17:16:33', 79);
INSERT INTO `sys_oper_log` VALUES (5934, '角色管理', 3, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-07-14 01:53:39\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2005,2030,2078,2083,2034,2088,2089,2040,2020,2114,2004,2007,2029,2006,2019,2080,2081,2085,2086,2091,2092,2095,2096,2098,2100,2101,2102,2103,2099,2104,2105,2106,2107,2021,2048,2049,2050,2051,2052,2053,2054,2055,2056,2057,2108,2109,2110,2111,2112,2113,100,1000,1001,1002,1003,1004,1005,1006,103,1016,1017,1018,1019],\"params\":{},\"roleId\":101,\"roleKey\":\"platAdmin\",\"roleName\":\"平台管理员\",\"roleSort\":3,\"status\":\"0\",\"updateBy\":\"admin\"}', '', 0, NULL, '2025-09-11 17:17:21', 151);
INSERT INTO `sys_oper_log` VALUES (5935, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582247991\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:17:28', 12);
INSERT INTO `sys_oper_log` VALUES (5936, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582259929\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:17:39', 7);
INSERT INTO `sys_oper_log` VALUES (5937, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582261308\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:17:41', 4);
INSERT INTO `sys_oper_log` VALUES (5938, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582262240\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:17:42', 6);
INSERT INTO `sys_oper_log` VALUES (5939, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582281623\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:01', 6);
INSERT INTO `sys_oper_log` VALUES (5940, '后台管理-成员知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgMemberList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/bgMemberList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582283101\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:03', 5);
INSERT INTO `sys_oper_log` VALUES (5941, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582284041\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:04', 4);
INSERT INTO `sys_oper_log` VALUES (5942, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582295082\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:15', 5);
INSERT INTO `sys_oper_log` VALUES (5943, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582296411\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:16', 11);
INSERT INTO `sys_oper_log` VALUES (5944, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582297203\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:17', 5);
INSERT INTO `sys_oper_log` VALUES (5945, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582299155\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:19', 5);
INSERT INTO `sys_oper_log` VALUES (5946, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582299648\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:19', 5);
INSERT INTO `sys_oper_log` VALUES (5947, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582300888\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:21', 4);
INSERT INTO `sys_oper_log` VALUES (5948, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582301486\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:21', 3);
INSERT INTO `sys_oper_log` VALUES (5949, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582302613\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:22', 6);
INSERT INTO `sys_oper_log` VALUES (5950, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582303163\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:23', 4);
INSERT INTO `sys_oper_log` VALUES (5951, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582303802\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:23', 4);
INSERT INTO `sys_oper_log` VALUES (5952, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:18:36', 13);
INSERT INTO `sys_oper_log` VALUES (5953, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'czj-admin', '保定市财政局', 225, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"czj-admin\",\"deptId\":226,\"nickName\":\"czj-bgs1\",\"params\":{},\"phonenumber\":\"15000000001\",\"postIds\":[],\"roleIds\":[2],\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"userId\":6,\"userName\":\"czj-bgs1\"}', '', 0, NULL, '2025-09-11 17:19:06', 153);
INSERT INTO `sys_oper_log` VALUES (5954, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:19:06', 15);
INSERT INTO `sys_oper_log` VALUES (5955, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582360463\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:19:20', 4);
INSERT INTO `sys_oper_log` VALUES (5956, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582361811\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:19:21', 5);
INSERT INTO `sys_oper_log` VALUES (5957, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582362699\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:19:22', 3);
INSERT INTO `sys_oper_log` VALUES (5958, '角色管理', 3, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-07-09 15:40:05\",\"dataScope\":\"5\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2005,2030,2078,2083,2034,2088,2089,2040,100,2004,2007,2006,2019,2080,2081,2085,2086,2091,2092,2095,2096,2098,2100,2101,2102,2103,2099,2104,2105,2106,2107,1000,1002],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":4,\"status\":\"0\",\"updateBy\":\"admin\"}', '', 0, NULL, '2025-09-11 17:19:55', 110);
INSERT INTO `sys_oper_log` VALUES (5959, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582424802\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:20:24', 3);
INSERT INTO `sys_oper_log` VALUES (5960, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582425958\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:20:25', 2);
INSERT INTO `sys_oper_log` VALUES (5961, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582428890\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:20:28', 4);
INSERT INTO `sys_oper_log` VALUES (5962, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757582429378\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:20:29', 4);
INSERT INTO `sys_oper_log` VALUES (5963, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:23:17', 8);
INSERT INTO `sys_oper_log` VALUES (5964, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:26:12', 6);
INSERT INTO `sys_oper_log` VALUES (5965, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757583660800\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:41:01', 5);
INSERT INTO `sys_oper_log` VALUES (5966, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"createBy\":\"admin\",\"deptId\":258,\"deptName\":\"财政局\",\"orderNum\":6,\"params\":{},\"parentId\":100,\"status\":\"0\"}', '', 0, NULL, '2025-09-11 17:41:28', 53);
INSERT INTO `sys_oper_log` VALUES (5967, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/258', '127.0.0.1', '内网IP', '258', '', 0, NULL, '2025-09-11 17:41:31', 80);
INSERT INTO `sys_oper_log` VALUES (5968, '部门管理', 4, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '数投科技', -1, '/system/dept/257', '127.0.0.1', '内网IP', '257', '', 0, NULL, '2025-09-11 17:41:34', 86);
INSERT INTO `sys_oper_log` VALUES (5969, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"createBy\":\"admin\",\"deptId\":259,\"deptName\":\"财政局\",\"orderNum\":5,\"params\":{},\"parentId\":100,\"status\":\"0\"}', '', 0, NULL, '2025-09-11 17:41:41', 61);
INSERT INTO `sys_oper_log` VALUES (5970, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757584144053\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:49:04', 7);
INSERT INTO `sys_oper_log` VALUES (5971, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757584157731\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:49:17', 3);
INSERT INTO `sys_oper_log` VALUES (5972, '后台模板公共知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757584158754\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:49:18', 3);
INSERT INTO `sys_oper_log` VALUES (5973, '模板文件导入', 7, 'com.ruoyi.dw.controller.DwTemplateController.importFile()', 'POST', 1, 'admin', '数投科技', -1, '/dw/template/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757584171455\",\"frontEnd\":\"1\"}', '', 0, NULL, '2025-09-11 17:49:32', 195);
INSERT INTO `sys_oper_log` VALUES (5974, '后台模板公共知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757584171994\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:49:32', 12);
INSERT INTO `sys_oper_log` VALUES (5975, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 17:50:12', 13);
INSERT INTO `sys_oper_log` VALUES (5976, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757585184213\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 18:06:24', 3);
INSERT INTO `sys_oper_log` VALUES (5977, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757585189338\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 18:06:29', 2);
INSERT INTO `sys_oper_log` VALUES (5978, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 19:31:43', 6);
INSERT INTO `sys_oper_log` VALUES (5979, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757590307086\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 19:31:47', 4);
INSERT INTO `sys_oper_log` VALUES (5980, '后台模板公共知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757590308012\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 19:31:48', 3);
INSERT INTO `sys_oper_log` VALUES (5981, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757592276336\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 20:04:36', 4);
INSERT INTO `sys_oper_log` VALUES (5982, '后台模板公共知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757592279360\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 20:04:39', 3);
INSERT INTO `sys_oper_log` VALUES (5983, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757596144725\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 21:09:04', 8);
INSERT INTO `sys_oper_log` VALUES (5984, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757598357381\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 21:45:57', 16);
INSERT INTO `sys_oper_log` VALUES (5985, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757598358721\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 21:45:58', 5);
INSERT INTO `sys_oper_log` VALUES (5986, '素材文件删除', 4, 'com.ruoyi.dw.controller.DwMaterialController.delete()', 'DELETE', 1, 'admin', '数投科技', -1, '/dw/material/1', '127.0.0.1', '内网IP', '1', '', 1, 'For input string: \"20302.0\"', '2025-09-11 21:46:04', 59);
INSERT INTO `sys_oper_log` VALUES (5987, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757598658964\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 21:50:59', 60);
INSERT INTO `sys_oper_log` VALUES (5988, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757598663772\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 21:51:03', 6);
INSERT INTO `sys_oper_log` VALUES (5989, '素材文件删除', 4, 'com.ruoyi.dw.controller.DwMaterialController.delete()', 'DELETE', 1, 'admin', '数投科技', -1, '/dw/material/1', '127.0.0.1', '内网IP', '1', '', 0, NULL, '2025-09-11 21:51:08', 110);
INSERT INTO `sys_oper_log` VALUES (5990, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757598668168\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 21:51:08', 6);
INSERT INTO `sys_oper_log` VALUES (5991, '素材文件删除', 4, 'com.ruoyi.dw.controller.DwMaterialController.delete()', 'DELETE', 1, 'admin', '数投科技', -1, '/dw/material/2', '127.0.0.1', '内网IP', '2', '', 0, NULL, '2025-09-11 21:51:43', 148);
INSERT INTO `sys_oper_log` VALUES (5992, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757598703082\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-11 21:51:43', 8);
INSERT INTO `sys_oper_log` VALUES (5993, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757646769051\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 11:12:49', 20);
INSERT INTO `sys_oper_log` VALUES (5994, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757646772717\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 11:12:53', 8);
INSERT INTO `sys_oper_log` VALUES (5995, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757647159948\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 11:19:19', 5);
INSERT INTO `sys_oper_log` VALUES (5996, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 11:19:23', 20);
INSERT INTO `sys_oper_log` VALUES (5997, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,247\",\"children\":[],\"deptId\":248,\"deptName\":\"市委办公室\",\"orderNum\":1,\"params\":{},\"parentId\":247,\"parentName\":\"保定市政府\",\"status\":\"0\",\"updateBy\":\"admin\"}', '', 0, NULL, '2025-09-12 11:19:29', 76);
INSERT INTO `sys_oper_log` VALUES (5998, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 20:30:15', 53);
INSERT INTO `sys_oper_log` VALUES (5999, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"czj-admin\",\"createTime\":\"2025-09-11 17:19:06\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,225\",\"children\":[],\"deptId\":226,\"deptName\":\"办公室\",\"orderNum\":1,\"params\":{},\"parentId\":225,\"status\":\"0\"},\"deptId\":226,\"email\":\"\",\"loginDate\":\"2025-09-11 17:20:21\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"czj-bgs2\",\"params\":{},\"phonenumber\":\"15000000001\",\"postIds\":[],\"roleIds\":[2],\"roleName\":\"普通角色\",\"roles\":[{\"admin\":false,\"dataScope\":\"5\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":4,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"updateBy\":\"admin\",\"userId\":6,\"userName\":\"czj-bgs2\"}', '', 1, 'nested exception is org.apache.ibatis.exceptions.PersistenceException: \r\n### Error updating database.  Cause: java.lang.NumberFormatException: For input string: \"czj-bgs2\"\r\n### The error may exist in file [F:\\IDEA\\DWSystem\\ruoyi-system\\target\\classes\\mapper\\system\\SysUserMapper.xml]\r\n### The error may involve com.ruoyi.system.mapper.SysUserMapper.updateUser\r\n### The error occurred while executing an update\r\n### Cause: java.lang.NumberFormatException: For input string: \"czj-bgs2\"', '2025-09-12 20:30:24', 104);
INSERT INTO `sys_oper_log` VALUES (6000, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"czj-admin\",\"createTime\":\"2025-09-11 17:19:06\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,225\",\"children\":[],\"deptId\":226,\"deptName\":\"办公室\",\"orderNum\":1,\"params\":{},\"parentId\":225,\"status\":\"0\"},\"deptId\":226,\"email\":\"\",\"loginDate\":\"2025-09-11 17:20:21\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"czj-bgs2\",\"params\":{},\"phonenumber\":\"15000000001\",\"postIds\":[],\"roleIds\":[2],\"roleName\":\"普通角色\",\"roles\":[{\"admin\":false,\"dataScope\":\"5\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":4,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"updateBy\":\"admin\",\"userId\":6,\"userName\":\"czj-bgs2\"}', '', 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near \'UPDATE gen_table_column SET\n			create_by = CASE WHEN create_by = \'czj-bgs1\' THEN\' at line 5\r\n### The error may exist in file [F:\\IDEA\\DWSystem\\ruoyi-system\\target\\classes\\mapper\\system\\SysUserMapper.xml]\r\n### The error may involve com.ruoyi.system.mapper.SysUserMapper.batchUpdateAllTablesByUserName-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE gen_table SET    create_by = CASE WHEN create_by = ? THEN ? ELSE create_by END,    update_by = CASE WHEN update_by = ? THEN ? ELSE update_by END    UPDATE gen_table_column SET    create_by = CASE WHEN create_by = ? THEN ? ELSE create_by END,    update_by = CASE WHEN update_by = ? THEN ? ELSE update_by END    UPDATE sys_config SET    create_by = CASE WHEN create_by = ? THEN ? ELSE create_by END,    update_by = CASE WHEN update_by = ? THEN ? ELSE update_by END    UPDATE sys_dept SET    create_by = CASE WHEN create_by = ? THEN ? ELSE create_by END,    update_by = CASE WHEN update_by = ? THEN ? ELSE update_by END    UPDATE sys_dict_data SET    create_by = CASE WHEN create_by = ? THEN ? ELSE create_by END,    update_by = CASE WHEN update_by = ? THEN ? ELSE update_by END    UPDATE sys_dict_type SET    create_by = CASE WHEN create_by = ? THEN ? ELSE create_by END,    update_by = CASE WHEN update_by = ? THEN ? ELSE update_by END    UPDATE sys_job SET    create_by = CASE WHEN create_by = ? THEN ? ELSE create_by END,    update_by = CASE WHEN update_by = ? THEN ? ELSE update_by END    UPDATE sys_logininfor SET    user_name = CASE WHEN user_name = ? THEN ? ELSE user_name END    UPDATE sys_menu SET    create_by = CASE WHEN create_by = ? THEN ? ELSE create_by END,    update_by = CASE WHEN update_by = ? THEN ? ELSE update_by END    UPDATE sys_notice SET    create_by = CASE WHEN create_by = ? THEN ? ELSE create_', '2025-09-12 20:33:09', 156);
INSERT INTO `sys_oper_log` VALUES (6001, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"czj-admin\",\"createTime\":\"2025-09-11 17:19:06\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,225\",\"children\":[],\"deptId\":226,\"deptName\":\"办公室\",\"orderNum\":1,\"params\":{},\"parentId\":225,\"status\":\"0\"},\"deptId\":226,\"email\":\"\",\"loginDate\":\"2025-09-11 17:20:21\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"czj-bgs2\",\"params\":{},\"phonenumber\":\"15000000001\",\"postIds\":[],\"roleIds\":[2],\"roleName\":\"普通角色\",\"roles\":[{\"admin\":false,\"dataScope\":\"5\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":4,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"updateBy\":\"admin\",\"userId\":6,\"userName\":\"czj-bgs2\"}', '', 0, NULL, '2025-09-12 20:37:58', 128);
INSERT INTO `sys_oper_log` VALUES (6002, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 20:37:58', 59);
INSERT INTO `sys_oper_log` VALUES (6003, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 20:38:13', 10);
INSERT INTO `sys_oper_log` VALUES (6004, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-09-11 17:12:08\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":225,\"deptName\":\"保定市财政局\",\"orderNum\":2,\"params\":{},\"parentId\":100,\"status\":\"0\"},\"deptId\":225,\"email\":\"\",\"loginDate\":\"2025-09-11 17:18:31\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"czj-admin2\",\"params\":{},\"phonenumber\":\"15000000001\",\"postIds\":[],\"roleIds\":[100],\"roleName\":\"单位管理者\",\"roles\":[{\"admin\":false,\"dataScope\":\"4\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"deptAdmin\",\"roleName\":\"单位管理者\",\"roleSort\":2,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"userId\":5,\"userName\":\"czj-admin2\"}', '', 0, NULL, '2025-09-12 20:38:37', 3);
INSERT INTO `sys_oper_log` VALUES (6005, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-09-11 17:12:08\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":225,\"deptName\":\"保定市财政局\",\"orderNum\":2,\"params\":{},\"parentId\":100,\"status\":\"0\"},\"deptId\":225,\"email\":\"\",\"loginDate\":\"2025-09-11 17:18:31\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"czj-admin2\",\"params\":{},\"phonenumber\":\"15000000000\",\"postIds\":[],\"roleIds\":[100],\"roleName\":\"单位管理者\",\"roles\":[{\"admin\":false,\"dataScope\":\"4\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"deptAdmin\",\"roleName\":\"单位管理者\",\"roleSort\":2,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"updateBy\":\"admin\",\"userId\":5,\"userName\":\"czj-admin2\"}', '', 0, NULL, '2025-09-12 20:38:49', 329);
INSERT INTO `sys_oper_log` VALUES (6006, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 20:38:49', 13);
INSERT INTO `sys_oper_log` VALUES (6007, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 20:45:39', 17);
INSERT INTO `sys_oper_log` VALUES (6008, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 20:51:01', 16);
INSERT INTO `sys_oper_log` VALUES (6009, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 22:28:28', 9);
INSERT INTO `sys_oper_log` VALUES (6010, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"czj-admin2\",\"createTime\":\"2025-09-11 17:19:06\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,225\",\"children\":[],\"deptId\":226,\"deptName\":\"办公室\",\"orderNum\":1,\"params\":{},\"parentId\":225,\"status\":\"0\"},\"deptId\":226,\"email\":\"\",\"loginDate\":\"2025-09-11 17:20:21\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"czj-bgs1\",\"params\":{},\"phonenumber\":\"15000000001\",\"postIds\":[],\"roleIds\":[2],\"roleName\":\"普通角色\",\"roles\":[{\"admin\":false,\"dataScope\":\"5\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":4,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"updateBy\":\"admin\",\"userId\":6,\"userName\":\"czj-bgs1\"}', '', 0, NULL, '2025-09-12 22:28:45', 153);
INSERT INTO `sys_oper_log` VALUES (6011, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 22:28:45', 7);
INSERT INTO `sys_oper_log` VALUES (6012, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-09-11 17:12:08\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":225,\"deptName\":\"保定市财政局\",\"orderNum\":2,\"params\":{},\"parentId\":100,\"status\":\"0\"},\"deptId\":225,\"email\":\"\",\"loginDate\":\"2025-09-11 17:18:31\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"czj-admin\",\"params\":{},\"phonenumber\":\"15000000000\",\"postIds\":[],\"roleIds\":[100],\"roleName\":\"单位管理者\",\"roles\":[{\"admin\":false,\"dataScope\":\"4\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"deptAdmin\",\"roleName\":\"单位管理者\",\"roleSort\":2,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"takeSpace\":null,\"unitId\":225,\"updateBy\":\"admin\",\"userId\":5,\"userName\":\"czj-admin\"}', '', 0, NULL, '2025-09-12 22:28:55', 403);
INSERT INTO `sys_oper_log` VALUES (6013, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 22:28:55', 9);
INSERT INTO `sys_oper_log` VALUES (6014, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757687355293\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 22:29:15', 6);
INSERT INTO `sys_oper_log` VALUES (6015, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 22:29:17', 14);
INSERT INTO `sys_oper_log` VALUES (6016, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757687384906\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 22:29:44', 4);
INSERT INTO `sys_oper_log` VALUES (6017, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757687985943\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 22:39:46', 3);
INSERT INTO `sys_oper_log` VALUES (6018, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757688760203\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-12 22:52:40', 10);
INSERT INTO `sys_oper_log` VALUES (6019, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757723828581\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-13 08:37:08', 4);
INSERT INTO `sys_oper_log` VALUES (6020, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757723907746\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-13 08:38:28', 5);
INSERT INTO `sys_oper_log` VALUES (6021, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834095157\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:14:55', 18);
INSERT INTO `sys_oper_log` VALUES (6022, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834098829\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:14:58', 2);
INSERT INTO `sys_oper_log` VALUES (6023, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834103255\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:15:03', 4);
INSERT INTO `sys_oper_log` VALUES (6024, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834112842\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:15:12', 7);
INSERT INTO `sys_oper_log` VALUES (6025, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834587913\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:23:07', 5);
INSERT INTO `sys_oper_log` VALUES (6026, '前台模板获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.selectionFileList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/selectionFileList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834640740\",\"pageSize\":\"10\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:24:01', 2);
INSERT INTO `sys_oper_log` VALUES (6027, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834886088\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:28:06', 49);
INSERT INTO `sys_oper_log` VALUES (6028, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834936608\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:28:56', 5);
INSERT INTO `sys_oper_log` VALUES (6029, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834937900\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:28:57', 13);
INSERT INTO `sys_oper_log` VALUES (6030, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834954507\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:29:14', 5);
INSERT INTO `sys_oper_log` VALUES (6031, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834956373\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:29:16', 4);
INSERT INTO `sys_oper_log` VALUES (6032, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 15:29:27', 3);
INSERT INTO `sys_oper_log` VALUES (6033, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757834985429\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 15:29:45', 459);
INSERT INTO `sys_oper_log` VALUES (6034, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 15:29:46', 13);
INSERT INTO `sys_oper_log` VALUES (6035, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834990149\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:29:50', 11);
INSERT INTO `sys_oper_log` VALUES (6036, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834991229\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:29:51', 9);
INSERT INTO `sys_oper_log` VALUES (6037, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 15:29:53', 3);
INSERT INTO `sys_oper_log` VALUES (6038, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834994472\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:29:54', 4);
INSERT INTO `sys_oper_log` VALUES (6039, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757834996238\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:29:56', 3);
INSERT INTO `sys_oper_log` VALUES (6040, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757835009997\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 15:30:10', 247);
INSERT INTO `sys_oper_log` VALUES (6041, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835010575\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:30:10', 6);
INSERT INTO `sys_oper_log` VALUES (6042, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 15:30:13', 3);
INSERT INTO `sys_oper_log` VALUES (6043, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835014813\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:30:14', 4);
INSERT INTO `sys_oper_log` VALUES (6044, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835016047\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:30:16', 6);
INSERT INTO `sys_oper_log` VALUES (6045, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835018896\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:30:18', 4);
INSERT INTO `sys_oper_log` VALUES (6046, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835019813\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:30:19', 5);
INSERT INTO `sys_oper_log` VALUES (6047, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835093366\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:31:33', 4);
INSERT INTO `sys_oper_log` VALUES (6048, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835094134\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:31:34', 4);
INSERT INTO `sys_oper_log` VALUES (6049, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835370984\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:36:11', 4);
INSERT INTO `sys_oper_log` VALUES (6050, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835373040\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:36:13', 5);
INSERT INTO `sys_oper_log` VALUES (6051, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试\"}', '', 0, NULL, '2025-09-14 15:36:14', 2);
INSERT INTO `sys_oper_log` VALUES (6052, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835380011\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:36:20', 9);
INSERT INTO `sys_oper_log` VALUES (6053, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 15:36:21', 3);
INSERT INTO `sys_oper_log` VALUES (6054, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835383752\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:36:24', 8);
INSERT INTO `sys_oper_log` VALUES (6055, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试\"}', '', 0, NULL, '2025-09-14 15:36:24', 3);
INSERT INTO `sys_oper_log` VALUES (6056, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835388538\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:36:28', 7);
INSERT INTO `sys_oper_log` VALUES (6057, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 15:36:30', 4);
INSERT INTO `sys_oper_log` VALUES (6058, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835392145\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:36:32', 5);
INSERT INTO `sys_oper_log` VALUES (6059, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试\"}', '', 0, NULL, '2025-09-14 15:36:34', 3);
INSERT INTO `sys_oper_log` VALUES (6060, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835713570\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:41:54', 60);
INSERT INTO `sys_oper_log` VALUES (6061, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835715010\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:41:55', 15);
INSERT INTO `sys_oper_log` VALUES (6062, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 15:41:57', 4);
INSERT INTO `sys_oper_log` VALUES (6063, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835718979\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:41:59', 8);
INSERT INTO `sys_oper_log` VALUES (6064, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 15:42:10', 6);
INSERT INTO `sys_oper_log` VALUES (6065, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835731698\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:42:11', 6);
INSERT INTO `sys_oper_log` VALUES (6066, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835777643\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:42:58', 44);
INSERT INTO `sys_oper_log` VALUES (6067, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835778210\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:42:58', 17);
INSERT INTO `sys_oper_log` VALUES (6068, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试\"}', '', 0, NULL, '2025-09-14 15:42:59', 8);
INSERT INTO `sys_oper_log` VALUES (6069, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835780922\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:43:01', 7);
INSERT INTO `sys_oper_log` VALUES (6070, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 15:43:02', 5);
INSERT INTO `sys_oper_log` VALUES (6071, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835783075\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:43:03', 7);
INSERT INTO `sys_oper_log` VALUES (6072, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835872331\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:44:32', 62);
INSERT INTO `sys_oper_log` VALUES (6073, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835873643\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:44:33', 15);
INSERT INTO `sys_oper_log` VALUES (6074, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835874604\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:44:34', 13);
INSERT INTO `sys_oper_log` VALUES (6075, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835876131\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:44:36', 10);
INSERT INTO `sys_oper_log` VALUES (6076, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757835876940\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 15:44:36', 9);
INSERT INTO `sys_oper_log` VALUES (6077, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837691648\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:14:51', 11);
INSERT INTO `sys_oper_log` VALUES (6078, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837693667\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:14:53', 8);
INSERT INTO `sys_oper_log` VALUES (6079, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837697899\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:14:58', 9);
INSERT INTO `sys_oper_log` VALUES (6080, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837698681\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:14:58', 4);
INSERT INTO `sys_oper_log` VALUES (6081, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837787466\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:16:27', 46);
INSERT INTO `sys_oper_log` VALUES (6082, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837788978\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:16:29', 26);
INSERT INTO `sys_oper_log` VALUES (6083, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837866099\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:17:46', 10);
INSERT INTO `sys_oper_log` VALUES (6084, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837867082\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:17:47', 7);
INSERT INTO `sys_oper_log` VALUES (6085, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试\"}', '', 0, NULL, '2025-09-14 16:17:48', 3);
INSERT INTO `sys_oper_log` VALUES (6086, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837869956\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:17:50', 5);
INSERT INTO `sys_oper_log` VALUES (6087, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 16:17:51', 4);
INSERT INTO `sys_oper_log` VALUES (6088, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837872587\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:17:52', 7);
INSERT INTO `sys_oper_log` VALUES (6089, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"测试\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 16:18:02', 79);
INSERT INTO `sys_oper_log` VALUES (6090, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837888795\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:18:08', 8);
INSERT INTO `sys_oper_log` VALUES (6091, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837889947\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:18:09', 7);
INSERT INTO `sys_oper_log` VALUES (6092, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"测试2\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 16:18:23', 72);
INSERT INTO `sys_oper_log` VALUES (6093, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试2\"}', '', 0, NULL, '2025-09-14 16:18:33', 3);
INSERT INTO `sys_oper_log` VALUES (6094, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837915499\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:18:35', 12);
INSERT INTO `sys_oper_log` VALUES (6095, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837917684\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:18:37', 4);
INSERT INTO `sys_oper_log` VALUES (6096, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837918411\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:18:38', 6);
INSERT INTO `sys_oper_log` VALUES (6097, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837919211\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:18:39', 5);
INSERT INTO `sys_oper_log` VALUES (6098, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"测试2\"}', '', 0, NULL, '2025-09-14 16:19:24', 3);
INSERT INTO `sys_oper_log` VALUES (6099, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837965932\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:19:25', 4);
INSERT INTO `sys_oper_log` VALUES (6100, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757837968375\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 16:19:28', 12);
INSERT INTO `sys_oper_log` VALUES (6101, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842340764\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:32:20', 48);
INSERT INTO `sys_oper_log` VALUES (6102, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842342012\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:32:22', 12);
INSERT INTO `sys_oper_log` VALUES (6103, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 17:32:24', 4);
INSERT INTO `sys_oper_log` VALUES (6104, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842347236\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:32:27', 4);
INSERT INTO `sys_oper_log` VALUES (6105, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"CES\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 17:32:33', 84);
INSERT INTO `sys_oper_log` VALUES (6106, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842355874\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:32:35', 6);
INSERT INTO `sys_oper_log` VALUES (6107, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842356513\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:32:36', 5);
INSERT INTO `sys_oper_log` VALUES (6108, '更新模型标签分析结果', 3, 'com.ruoyi.dw.controller.DwMaterialController.updateFolderName()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/updateFolderName', '127.0.0.1', '内网IP', '{\"folderName\":\"CES1\",\"oldFolderName\":\"CES\"}', '', 0, NULL, '2025-09-14 17:32:41', 86);
INSERT INTO `sys_oper_log` VALUES (6109, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"CES1\"}', '', 0, NULL, '2025-09-14 17:32:43', 3);
INSERT INTO `sys_oper_log` VALUES (6110, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757842370627\",\"folderName\":\"CES1\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 17:32:51', 525);
INSERT INTO `sys_oper_log` VALUES (6111, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"CES1\"}', '', 0, NULL, '2025-09-14 17:32:51', 4);
INSERT INTO `sys_oper_log` VALUES (6112, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842372849\",\"pageSize\":\"12\",\"type\":\"1\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:32:53', 8);
INSERT INTO `sys_oper_log` VALUES (6113, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842374608\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:32:54', 4);
INSERT INTO `sys_oper_log` VALUES (6114, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"CES1\"}', '', 0, NULL, '2025-09-14 17:32:56', 5);
INSERT INTO `sys_oper_log` VALUES (6115, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842376973\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:32:57', 6);
INSERT INTO `sys_oper_log` VALUES (6116, '素材文件删除', 4, 'com.ruoyi.dw.controller.DwMaterialController.deleteFolder()', 'DELETE', 1, 'czj-bgs1', '办公室', 225, '/dw/material/deleteFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"CES1\"}', '', 1, 'java.lang.Long cannot be cast to java.lang.String', '2025-09-14 17:33:05', 4);
INSERT INTO `sys_oper_log` VALUES (6117, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842391735\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:33:11', 5);
INSERT INTO `sys_oper_log` VALUES (6118, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757842393073\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 17:33:13', 4);
INSERT INTO `sys_oper_log` VALUES (6119, '素材文件删除', 4, 'com.ruoyi.dw.controller.DwMaterialController.deleteFolder()', 'DELETE', 1, 'czj-bgs1', '办公室', 225, '/dw/material/deleteFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"CES1\"}', '', 0, NULL, '2025-09-14 17:33:17', 99);
INSERT INTO `sys_oper_log` VALUES (6120, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757852999717\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:29:59', 43);
INSERT INTO `sys_oper_log` VALUES (6121, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757853000555\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:30:00', 12);
INSERT INTO `sys_oper_log` VALUES (6122, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 1, 'nested exception is org.apache.ibatis.type.TypeException: Could not set parameters for mapping: ParameterMapping{property=\'userid\', mode=IN, javaType=class java.lang.String, jdbcType=null, numericScale=null, resultMapId=\'null\', jdbcTypeName=\'null\', expression=\'null\'}. Cause: org.apache.ibatis.type.TypeException: Error setting non null for parameter #2 with JdbcType null . Try setting a different JdbcType for this parameter or a different configuration property. Cause: java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.String', '2025-09-14 20:30:08', 8);
INSERT INTO `sys_oper_log` VALUES (6123, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 20:36:40', 44);
INSERT INTO `sys_oper_log` VALUES (6124, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'czj-admin', '保定市财政局', 225, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:37:27', 22);
INSERT INTO `sys_oper_log` VALUES (6125, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:37:39', 24);
INSERT INTO `sys_oper_log` VALUES (6126, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757853807831\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:43:27', 13);
INSERT INTO `sys_oper_log` VALUES (6127, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757853809511\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:43:29', 5);
INSERT INTO `sys_oper_log` VALUES (6128, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告\",\"frontEnd\":\"0\"}', '', 1, 'Invalid bound statement (not found): com.ruoyi.dw.mapper.DwMaterialMapper.checkfolderNameUnique', '2025-09-14 20:43:39', 5);
INSERT INTO `sys_oper_log` VALUES (6129, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告\",\"frontEnd\":\"0\"}', '', 1, 'Invalid bound statement (not found): com.ruoyi.dw.mapper.DwMaterialMapper.checkfolderNameUnique', '2025-09-14 20:43:44', 2);
INSERT INTO `sys_oper_log` VALUES (6130, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告\",\"frontEnd\":\"0\"}', '', 1, 'Invalid bound statement (not found): com.ruoyi.dw.mapper.DwMaterialMapper.checkfolderNameUnique', '2025-09-14 20:46:45', 30);
INSERT INTO `sys_oper_log` VALUES (6131, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:48:21', 125);
INSERT INTO `sys_oper_log` VALUES (6132, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告\"}', '', 0, NULL, '2025-09-14 20:48:24', 33);
INSERT INTO `sys_oper_log` VALUES (6133, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854126450\",\"folderName\":\"政府报告\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:48:47', 494);
INSERT INTO `sys_oper_log` VALUES (6134, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854126450\",\"folderName\":\"政府报告\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:48:47', 299);
INSERT INTO `sys_oper_log` VALUES (6135, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854126450\",\"folderName\":\"政府报告\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:48:48', 431);
INSERT INTO `sys_oper_log` VALUES (6136, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告\"}', '', 0, NULL, '2025-09-14 20:48:48', 7);
INSERT INTO `sys_oper_log` VALUES (6137, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757854131690\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:48:52', 17);
INSERT INTO `sys_oper_log` VALUES (6138, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告\"}', '', 0, NULL, '2025-09-14 20:48:52', 4);
INSERT INTO `sys_oper_log` VALUES (6139, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757854134436\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:48:54', 13);
INSERT INTO `sys_oper_log` VALUES (6140, '更新模型标签分析结果', 3, 'com.ruoyi.dw.controller.DwMaterialController.updateFolderName()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/updateFolderName', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告1\",\"oldFolderName\":\"政府报告\"}', '', 0, NULL, '2025-09-14 20:48:59', 55);
INSERT INTO `sys_oper_log` VALUES (6141, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告1\"}', '', 0, NULL, '2025-09-14 20:49:11', 4);
INSERT INTO `sys_oper_log` VALUES (6142, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757854153235\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:49:13', 6);
INSERT INTO `sys_oper_log` VALUES (6143, '素材文件删除', 4, 'com.ruoyi.dw.controller.DwMaterialController.deleteFolder()', 'DELETE', 1, 'czj-bgs1', '办公室', 225, '/dw/material/deleteFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告1\"}', '', 1, 'java.lang.Long cannot be cast to java.lang.String', '2025-09-14 20:49:17', 3);
INSERT INTO `sys_oper_log` VALUES (6144, '素材文件删除', 4, 'com.ruoyi.dw.controller.DwMaterialController.deleteFolder()', 'DELETE', 1, 'czj-bgs1', '办公室', 225, '/dw/material/deleteFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"政府报告1\"}', '', 0, NULL, '2025-09-14 20:52:27', 177);
INSERT INTO `sys_oper_log` VALUES (6145, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 20:52:42', 37);
INSERT INTO `sys_oper_log` VALUES (6146, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757854363734\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:52:43', 11);
INSERT INTO `sys_oper_log` VALUES (6147, '素材文件删除', 4, 'com.ruoyi.dw.controller.DwMaterialController.deleteFolder()', 'DELETE', 1, 'czj-bgs1', '办公室', 225, '/dw/material/deleteFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 20:52:47', 127);
INSERT INTO `sys_oper_log` VALUES (6148, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:53:26', 85);
INSERT INTO `sys_oper_log` VALUES (6149, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 20:53:27', 7);
INSERT INTO `sys_oper_log` VALUES (6150, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854426684\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:53:47', 449);
INSERT INTO `sys_oper_log` VALUES (6151, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854426684\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:53:47', 260);
INSERT INTO `sys_oper_log` VALUES (6152, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854426684\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:53:48', 617);
INSERT INTO `sys_oper_log` VALUES (6153, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854426684\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:53:49', 287);
INSERT INTO `sys_oper_log` VALUES (6154, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854426684\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:53:49', 254);
INSERT INTO `sys_oper_log` VALUES (6155, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854426684\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:53:49', 243);
INSERT INTO `sys_oper_log` VALUES (6156, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854426684\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:53:50', 341);
INSERT INTO `sys_oper_log` VALUES (6157, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757854426684\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 20:53:50', 346);
INSERT INTO `sys_oper_log` VALUES (6158, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 20:53:51', 5);
INSERT INTO `sys_oper_log` VALUES (6159, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757854434670\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:53:54', 4);
INSERT INTO `sys_oper_log` VALUES (6160, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 20:54:12', 6);
INSERT INTO `sys_oper_log` VALUES (6161, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757854455397\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 20:54:15', 5);
INSERT INTO `sys_oper_log` VALUES (6162, '素材文件删除', 4, 'com.ruoyi.dw.controller.DwMaterialController.deleteFolder()', 'DELETE', 1, 'czj-bgs1', '办公室', 225, '/dw/material/deleteFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 20:54:20', 158);
INSERT INTO `sys_oper_log` VALUES (6163, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757855188659\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:06:29', 49);
INSERT INTO `sys_oper_log` VALUES (6164, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757855393988\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:09:54', 55);
INSERT INTO `sys_oper_log` VALUES (6165, '素材文件夹创建', 7, 'com.ruoyi.dw.controller.DwMaterialController.createFolder()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/createFolder', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 21:10:05', 99);
INSERT INTO `sys_oper_log` VALUES (6166, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 21:10:06', 3);
INSERT INTO `sys_oper_log` VALUES (6167, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757855410076\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:10:10', 16);
INSERT INTO `sys_oper_log` VALUES (6168, '前台模板知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/template/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757855411276\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:10:11', 6);
INSERT INTO `sys_oper_log` VALUES (6169, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757855411990\",\"pageSize\":\"12\",\"type\":\"2\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:10:12', 5);
INSERT INTO `sys_oper_log` VALUES (6170, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 21:10:13', 3);
INSERT INTO `sys_oper_log` VALUES (6171, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757855421846\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 21:10:22', 587);
INSERT INTO `sys_oper_log` VALUES (6172, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757855421846\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 21:10:23', 212);
INSERT INTO `sys_oper_log` VALUES (6173, '素材文件导入', 7, 'com.ruoyi.dw.controller.DwMaterialController.importFile()', 'POST', 1, 'czj-bgs1', '办公室', 225, '/dw/material/importFile', '127.0.0.1', '内网IP', '{\"uploadToken\":\"1757855421846\",\"folderName\":\"讲话稿\",\"frontEnd\":\"0\"}', '', 0, NULL, '2025-09-14 21:10:23', 244);
INSERT INTO `sys_oper_log` VALUES (6174, '前台个人素材文件夹', 0, 'com.ruoyi.dw.controller.DwMaterialController.getFolderMeterial()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/getFolderMeterial', '127.0.0.1', '内网IP', '{\"folderName\":\"讲话稿\"}', '', 0, NULL, '2025-09-14 21:10:23', 4);
INSERT INTO `sys_oper_log` VALUES (6175, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757856126882\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:22:07', 51);
INSERT INTO `sys_oper_log` VALUES (6176, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757856942618\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:35:42', 11);
INSERT INTO `sys_oper_log` VALUES (6177, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757857735905\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:48:55', 6);
INSERT INTO `sys_oper_log` VALUES (6178, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757857768508\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:49:28', 5);
INSERT INTO `sys_oper_log` VALUES (6179, '前台-知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.knowledgeBaseList()', 'GET', 1, 'czj-bgs1', '办公室', 225, '/dw/material/knowledgeBaseList', '127.0.0.1', '内网IP', '{\"_t\":\"1757857777271\",\"pageSize\":\"12\",\"type\":\"0\",\"pageNum\":\"1\",\"content\":\"保定\"}', '', 0, NULL, '2025-09-14 21:49:37', 8);
INSERT INTO `sys_oper_log` VALUES (6180, '后台管理-公共知识库', 0, 'com.ruoyi.dw.controller.DwMaterialController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/material/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757857931171\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:52:11', 6);
INSERT INTO `sys_oper_log` VALUES (6181, '后台模板公共知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757857932845\",\"pageSize\":\"12\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-09-14 21:52:12', 8);
INSERT INTO `sys_oper_log` VALUES (6182, '后台模板公共知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757857941096\",\"pageSize\":\"12\",\"pageNum\":\"1\",\"content\":\"调研\"}', '', 0, NULL, '2025-09-14 21:52:21', 5);
INSERT INTO `sys_oper_log` VALUES (6183, '后台模板公共知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757858027048\",\"pageSize\":\"12\",\"pageNum\":\"1\",\"content\":\"举报\"}', '', 0, NULL, '2025-09-14 21:53:47', 5);
INSERT INTO `sys_oper_log` VALUES (6184, '后台模板公共知识库获取', 0, 'com.ruoyi.dw.controller.DwTemplateController.bgPublicList()', 'GET', 1, 'admin', '数投科技', -1, '/dw/template/bgPublicList', '127.0.0.1', '内网IP', '{\"_t\":\"1757858111888\",\"pageSize\":\"12\",\"pageNum\":\"1\",\"content\":\"举报\"}', '', 0, NULL, '2025-09-14 21:55:12', 51);
INSERT INTO `sys_oper_log` VALUES (6185, '用户列表', 6, 'com.ruoyi.web.controller.system.SysUserController.list()', 'GET', 1, 'admin', '数投科技', -1, '/system/user/list', '127.0.0.1', '内网IP', '{\"pageSize\":\"10\",\"pageNum\":\"1\"}', '', 0, NULL, '2025-10-18 08:27:55', 19);
INSERT INTO `sys_oper_log` VALUES (6186, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '数投科技', -1, '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"tool/swagger/index\",\"createTime\":\"2025-07-09 15:40:06\",\"icon\":\"swagger\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":117,\"menuName\":\"系统接口\",\"menuType\":\"C\",\"orderNum\":3,\"params\":{},\"parentId\":3,\"path\":\"swagger\",\"perms\":\"tool:swagger:list\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '', 0, NULL, '2025-10-18 08:30:37', 127);
INSERT INTO `sys_oper_log` VALUES (6187, '代码生成', 7, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '数投科技', -1, '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"tdw_outlines,tdw_bids,tdw_contents\"}', '', 0, NULL, '2026-03-27 16:31:00', 143);
INSERT INTO `sys_oper_log` VALUES (6188, '代码生成', 9, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '数投科技', -1, '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"tdw_bids,tdw_contents,tdw_outlines\"}', '', 0, NULL, '2026-03-27 16:31:46', 239);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int(0) NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(0) NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2025-07-09 15:40:05', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 4, '5', 1, 1, '0', '0', 'admin', '2025-07-09 15:40:05', 'admin', '2025-09-11 17:19:54', '普通角色');
INSERT INTO `sys_role` VALUES (100, '单位管理者', 'deptAdmin', 2, '4', 1, 1, '0', '0', 'admin', '2025-07-14 01:48:10', 'admin', '2025-09-11 17:16:33', NULL);
INSERT INTO `sys_role` VALUES (101, '平台管理员', 'platAdmin', 3, '1', 1, 1, '0', '0', 'admin', '2025-07-14 01:53:39', 'admin', '2025-09-11 17:17:21', NULL);
INSERT INTO `sys_role` VALUES (102, '管理者', 'platAdmin', 0, '1', 1, 1, '0', '2', 'admin', '2025-08-21 14:27:20', 'admin', '2025-08-25 15:05:27', NULL);
INSERT INTO `sys_role` VALUES (103, '普通账号体验', 'common', 0, '5', 1, 1, '0', '0', 'admin', '2025-08-21 15:12:18', 'st-admin', '2025-08-26 20:26:23', NULL);
INSERT INTO `sys_role` VALUES (104, 'test', 'common', 0, '1', 1, 1, '0', '2', 'admin', '2025-08-22 11:45:27', '', NULL, NULL);
INSERT INTO `sys_role` VALUES (105, 'test', 'common', 0, '1', 1, 1, '0', '2', 'admin', '2025-08-22 11:46:52', '', NULL, NULL);
INSERT INTO `sys_role` VALUES (106, 'test', 'common', 7, '1', 1, 1, '0', '2', 'admin', '2025-08-22 11:48:44', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(0) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色和部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(0) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (2, 100);
INSERT INTO `sys_role_menu` VALUES (2, 1000);
INSERT INTO `sys_role_menu` VALUES (2, 1002);
INSERT INTO `sys_role_menu` VALUES (2, 2004);
INSERT INTO `sys_role_menu` VALUES (2, 2005);
INSERT INTO `sys_role_menu` VALUES (2, 2006);
INSERT INTO `sys_role_menu` VALUES (2, 2007);
INSERT INTO `sys_role_menu` VALUES (2, 2019);
INSERT INTO `sys_role_menu` VALUES (2, 2030);
INSERT INTO `sys_role_menu` VALUES (2, 2034);
INSERT INTO `sys_role_menu` VALUES (2, 2040);
INSERT INTO `sys_role_menu` VALUES (2, 2078);
INSERT INTO `sys_role_menu` VALUES (2, 2080);
INSERT INTO `sys_role_menu` VALUES (2, 2081);
INSERT INTO `sys_role_menu` VALUES (2, 2083);
INSERT INTO `sys_role_menu` VALUES (2, 2085);
INSERT INTO `sys_role_menu` VALUES (2, 2086);
INSERT INTO `sys_role_menu` VALUES (2, 2088);
INSERT INTO `sys_role_menu` VALUES (2, 2089);
INSERT INTO `sys_role_menu` VALUES (2, 2091);
INSERT INTO `sys_role_menu` VALUES (2, 2092);
INSERT INTO `sys_role_menu` VALUES (2, 2095);
INSERT INTO `sys_role_menu` VALUES (2, 2096);
INSERT INTO `sys_role_menu` VALUES (2, 2098);
INSERT INTO `sys_role_menu` VALUES (2, 2099);
INSERT INTO `sys_role_menu` VALUES (2, 2100);
INSERT INTO `sys_role_menu` VALUES (2, 2101);
INSERT INTO `sys_role_menu` VALUES (2, 2102);
INSERT INTO `sys_role_menu` VALUES (2, 2103);
INSERT INTO `sys_role_menu` VALUES (2, 2104);
INSERT INTO `sys_role_menu` VALUES (2, 2105);
INSERT INTO `sys_role_menu` VALUES (2, 2106);
INSERT INTO `sys_role_menu` VALUES (2, 2107);
INSERT INTO `sys_role_menu` VALUES (100, 100);
INSERT INTO `sys_role_menu` VALUES (100, 103);
INSERT INTO `sys_role_menu` VALUES (100, 1000);
INSERT INTO `sys_role_menu` VALUES (100, 1001);
INSERT INTO `sys_role_menu` VALUES (100, 1002);
INSERT INTO `sys_role_menu` VALUES (100, 1003);
INSERT INTO `sys_role_menu` VALUES (100, 1004);
INSERT INTO `sys_role_menu` VALUES (100, 1005);
INSERT INTO `sys_role_menu` VALUES (100, 1006);
INSERT INTO `sys_role_menu` VALUES (100, 1016);
INSERT INTO `sys_role_menu` VALUES (100, 1017);
INSERT INTO `sys_role_menu` VALUES (100, 1018);
INSERT INTO `sys_role_menu` VALUES (100, 1019);
INSERT INTO `sys_role_menu` VALUES (100, 2004);
INSERT INTO `sys_role_menu` VALUES (100, 2005);
INSERT INTO `sys_role_menu` VALUES (100, 2006);
INSERT INTO `sys_role_menu` VALUES (100, 2007);
INSERT INTO `sys_role_menu` VALUES (100, 2019);
INSERT INTO `sys_role_menu` VALUES (100, 2020);
INSERT INTO `sys_role_menu` VALUES (100, 2021);
INSERT INTO `sys_role_menu` VALUES (100, 2022);
INSERT INTO `sys_role_menu` VALUES (100, 2023);
INSERT INTO `sys_role_menu` VALUES (100, 2029);
INSERT INTO `sys_role_menu` VALUES (100, 2030);
INSERT INTO `sys_role_menu` VALUES (100, 2034);
INSERT INTO `sys_role_menu` VALUES (100, 2040);
INSERT INTO `sys_role_menu` VALUES (100, 2041);
INSERT INTO `sys_role_menu` VALUES (100, 2042);
INSERT INTO `sys_role_menu` VALUES (100, 2043);
INSERT INTO `sys_role_menu` VALUES (100, 2044);
INSERT INTO `sys_role_menu` VALUES (100, 2048);
INSERT INTO `sys_role_menu` VALUES (100, 2050);
INSERT INTO `sys_role_menu` VALUES (100, 2051);
INSERT INTO `sys_role_menu` VALUES (100, 2053);
INSERT INTO `sys_role_menu` VALUES (100, 2055);
INSERT INTO `sys_role_menu` VALUES (100, 2056);
INSERT INTO `sys_role_menu` VALUES (100, 2058);
INSERT INTO `sys_role_menu` VALUES (100, 2059);
INSERT INTO `sys_role_menu` VALUES (100, 2060);
INSERT INTO `sys_role_menu` VALUES (100, 2061);
INSERT INTO `sys_role_menu` VALUES (100, 2062);
INSERT INTO `sys_role_menu` VALUES (100, 2063);
INSERT INTO `sys_role_menu` VALUES (100, 2064);
INSERT INTO `sys_role_menu` VALUES (100, 2065);
INSERT INTO `sys_role_menu` VALUES (100, 2066);
INSERT INTO `sys_role_menu` VALUES (100, 2067);
INSERT INTO `sys_role_menu` VALUES (100, 2068);
INSERT INTO `sys_role_menu` VALUES (100, 2070);
INSERT INTO `sys_role_menu` VALUES (100, 2071);
INSERT INTO `sys_role_menu` VALUES (100, 2073);
INSERT INTO `sys_role_menu` VALUES (100, 2075);
INSERT INTO `sys_role_menu` VALUES (100, 2076);
INSERT INTO `sys_role_menu` VALUES (100, 2078);
INSERT INTO `sys_role_menu` VALUES (100, 2080);
INSERT INTO `sys_role_menu` VALUES (100, 2081);
INSERT INTO `sys_role_menu` VALUES (100, 2083);
INSERT INTO `sys_role_menu` VALUES (100, 2085);
INSERT INTO `sys_role_menu` VALUES (100, 2086);
INSERT INTO `sys_role_menu` VALUES (100, 2088);
INSERT INTO `sys_role_menu` VALUES (100, 2089);
INSERT INTO `sys_role_menu` VALUES (100, 2091);
INSERT INTO `sys_role_menu` VALUES (100, 2092);
INSERT INTO `sys_role_menu` VALUES (100, 2095);
INSERT INTO `sys_role_menu` VALUES (100, 2096);
INSERT INTO `sys_role_menu` VALUES (100, 2098);
INSERT INTO `sys_role_menu` VALUES (100, 2099);
INSERT INTO `sys_role_menu` VALUES (100, 2100);
INSERT INTO `sys_role_menu` VALUES (100, 2101);
INSERT INTO `sys_role_menu` VALUES (100, 2102);
INSERT INTO `sys_role_menu` VALUES (100, 2103);
INSERT INTO `sys_role_menu` VALUES (100, 2104);
INSERT INTO `sys_role_menu` VALUES (100, 2105);
INSERT INTO `sys_role_menu` VALUES (100, 2106);
INSERT INTO `sys_role_menu` VALUES (100, 2107);
INSERT INTO `sys_role_menu` VALUES (100, 2114);
INSERT INTO `sys_role_menu` VALUES (101, 100);
INSERT INTO `sys_role_menu` VALUES (101, 103);
INSERT INTO `sys_role_menu` VALUES (101, 1000);
INSERT INTO `sys_role_menu` VALUES (101, 1001);
INSERT INTO `sys_role_menu` VALUES (101, 1002);
INSERT INTO `sys_role_menu` VALUES (101, 1003);
INSERT INTO `sys_role_menu` VALUES (101, 1004);
INSERT INTO `sys_role_menu` VALUES (101, 1005);
INSERT INTO `sys_role_menu` VALUES (101, 1006);
INSERT INTO `sys_role_menu` VALUES (101, 1016);
INSERT INTO `sys_role_menu` VALUES (101, 1017);
INSERT INTO `sys_role_menu` VALUES (101, 1018);
INSERT INTO `sys_role_menu` VALUES (101, 1019);
INSERT INTO `sys_role_menu` VALUES (101, 2004);
INSERT INTO `sys_role_menu` VALUES (101, 2005);
INSERT INTO `sys_role_menu` VALUES (101, 2006);
INSERT INTO `sys_role_menu` VALUES (101, 2007);
INSERT INTO `sys_role_menu` VALUES (101, 2019);
INSERT INTO `sys_role_menu` VALUES (101, 2020);
INSERT INTO `sys_role_menu` VALUES (101, 2021);
INSERT INTO `sys_role_menu` VALUES (101, 2029);
INSERT INTO `sys_role_menu` VALUES (101, 2030);
INSERT INTO `sys_role_menu` VALUES (101, 2034);
INSERT INTO `sys_role_menu` VALUES (101, 2040);
INSERT INTO `sys_role_menu` VALUES (101, 2048);
INSERT INTO `sys_role_menu` VALUES (101, 2049);
INSERT INTO `sys_role_menu` VALUES (101, 2050);
INSERT INTO `sys_role_menu` VALUES (101, 2051);
INSERT INTO `sys_role_menu` VALUES (101, 2052);
INSERT INTO `sys_role_menu` VALUES (101, 2053);
INSERT INTO `sys_role_menu` VALUES (101, 2054);
INSERT INTO `sys_role_menu` VALUES (101, 2055);
INSERT INTO `sys_role_menu` VALUES (101, 2056);
INSERT INTO `sys_role_menu` VALUES (101, 2057);
INSERT INTO `sys_role_menu` VALUES (101, 2078);
INSERT INTO `sys_role_menu` VALUES (101, 2080);
INSERT INTO `sys_role_menu` VALUES (101, 2081);
INSERT INTO `sys_role_menu` VALUES (101, 2083);
INSERT INTO `sys_role_menu` VALUES (101, 2085);
INSERT INTO `sys_role_menu` VALUES (101, 2086);
INSERT INTO `sys_role_menu` VALUES (101, 2088);
INSERT INTO `sys_role_menu` VALUES (101, 2089);
INSERT INTO `sys_role_menu` VALUES (101, 2091);
INSERT INTO `sys_role_menu` VALUES (101, 2092);
INSERT INTO `sys_role_menu` VALUES (101, 2095);
INSERT INTO `sys_role_menu` VALUES (101, 2096);
INSERT INTO `sys_role_menu` VALUES (101, 2098);
INSERT INTO `sys_role_menu` VALUES (101, 2099);
INSERT INTO `sys_role_menu` VALUES (101, 2100);
INSERT INTO `sys_role_menu` VALUES (101, 2101);
INSERT INTO `sys_role_menu` VALUES (101, 2102);
INSERT INTO `sys_role_menu` VALUES (101, 2103);
INSERT INTO `sys_role_menu` VALUES (101, 2104);
INSERT INTO `sys_role_menu` VALUES (101, 2105);
INSERT INTO `sys_role_menu` VALUES (101, 2106);
INSERT INTO `sys_role_menu` VALUES (101, 2107);
INSERT INTO `sys_role_menu` VALUES (101, 2108);
INSERT INTO `sys_role_menu` VALUES (101, 2109);
INSERT INTO `sys_role_menu` VALUES (101, 2110);
INSERT INTO `sys_role_menu` VALUES (101, 2111);
INSERT INTO `sys_role_menu` VALUES (101, 2112);
INSERT INTO `sys_role_menu` VALUES (101, 2113);
INSERT INTO `sys_role_menu` VALUES (101, 2114);
INSERT INTO `sys_role_menu` VALUES (103, 2004);
INSERT INTO `sys_role_menu` VALUES (103, 2006);
INSERT INTO `sys_role_menu` VALUES (103, 2007);
INSERT INTO `sys_role_menu` VALUES (103, 2019);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(0) DEFAULT NULL COMMENT '部门ID',
  `unit_id` bigint(0) DEFAULT NULL COMMENT '单位ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime(0) DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime(0) DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `invocation_num` bigint(0) NOT NULL DEFAULT 0,
  `generatedoc_num` bigint(0) NOT NULL DEFAULT 0,
  `material_num` bigint(0) NOT NULL DEFAULT 0,
  `take_space` double NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 100, -1, 'admin', '系统超级管理员', '00', 'ry@163.com', '15888888887', '1', '/profile/avatar/2025/07/16/57a73a4c7d4846f9adf19d878845e373.png', '$2a$10$TQtQvbf4.3hUFv0WgjbDC.dd3tYj9jNqqN10tyf8VTiuw97UE3T3K', '0', '0', '127.0.0.1', '2026-03-27 16:30:43', '2025-07-13 12:06:58', 'admin', '2025-07-09 15:40:03', '', '2026-03-27 16:30:43', '管理员', 31, 0, 0, 10176);
INSERT INTO `sys_user` VALUES (5, 225, 225, 'czj-admin', 'czj-admin', '00', '', '15000000000', '0', '', '$2a$10$qP8.vOEVkjlJDmzVG9PWUe5K8WDA3w9cjmnZPeq9rdCdic/vly5jy', '0', '0', '127.0.0.1', '2025-09-14 20:37:22', NULL, 'admin', '2025-09-11 17:12:08', 'admin', '2025-09-14 20:37:22', NULL, 0, 0, 0, 0);
INSERT INTO `sys_user` VALUES (6, 226, 225, 'czj-bgs1', 'czj-bgs1', '00', '', '15000000001', '0', '', '$2a$10$JxA773QQyeWNkCBfa47MnOmitktQp6HItz13UwDciBstd8hP61SyS', '0', '0', '127.0.0.1', '2025-09-14 20:43:25', NULL, 'czj-admin', '2025-09-11 17:19:06', 'admin', '2025-09-14 20:43:24', NULL, 0, 0, 6, 171483);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `post_id` bigint(0) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户和角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (5, 100);
INSERT INTO `sys_user_role` VALUES (6, 2);

-- ----------------------------
-- Table structure for tdw_bids
-- ----------------------------
DROP TABLE IF EXISTS `tdw_bids`;
CREATE TABLE `tdw_bids`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '表示名称',
  `template_id` int(0) DEFAULT NULL COMMENT '关联的模板ID',
  `status` int(0) DEFAULT NULL COMMENT '状态：1-草稿，2-已完成',
  `created_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `userid` int(0) DEFAULT NULL COMMENT '用户id',
  `deptid` int(0) DEFAULT NULL COMMENT '部门id',
  `unitid` int(0) DEFAULT NULL COMMENT '单位id',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tdw_bids
-- ----------------------------
INSERT INTO `tdw_bids` VALUES (1001, '2026年XX市智慧城市大脑建设项目投标书', 50, NULL, '2026-03-27 09:00:00', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for tdw_contents
-- ----------------------------
DROP TABLE IF EXISTS `tdw_contents`;
CREATE TABLE `tdw_contents`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '内容块ID',
  `outline_id` bigint(0) NOT NULL COMMENT '关联的大纲节点ID（永远不变，大纲移动也不用改）',
  `content_type` tinyint(0) NOT NULL COMMENT '内容类型：1=文本，2=表格，3=图片',
  `content` json NOT NULL COMMENT '具体内容，用MySQL原生JSON类型，比text性能高、支持校验',
  `sort_order` int(0) NOT NULL COMMENT '节点下内容的显示顺序，用于混排排序',
  `create_time` datetime(0) DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_outline_id`(`outline_id`) USING BTREE,
  CONSTRAINT `fk_content_outline` FOREIGN KEY (`outline_id`) REFERENCES `tdw_outlines` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内容块表，支持文本/表格/图片混排，随意增删改' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tdw_contents
-- ----------------------------
INSERT INTO `tdw_contents` VALUES (1, 111, 1, '{\"text\": \"近年来，国家大力推进新型智慧城市建设，出台了《关于全面推进城市数字化转型的指导意见》，要求各地加快城市大脑建设。\"}', 1, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (2, 112, 1, '{\"text\": \"目前该市各委办局数据孤岛现象严重，缺乏统一的数据交换与治理中枢，难以支撑跨部门协同指挥。\"}', 1, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (3, 112, 3, '{\"url\": \"https://img.example.com/data-island.png\", \"name\": \"当前城市数据孤岛现状示意图\", \"width\": 1920, \"height\": 1080}', 2, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (4, 121, 1, '{\"text\": \"系统需实现政务服务“一网通办”与城市运行“一网统管”，涵盖交通、安防、环保等8大核心领域的业务协同。\"}', 1, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (5, 122, 2, '{\"name\": \"性能指标清单\", \"rows\": [[\"并发用户数\", \">= 10000\", \"压测报告\"], [\"API响应时间\", \"<= 50ms\", \"线上监控\"], [\"系统可用性\", \"99.99%\", \"SLA协议\"]], \"headers\": [\"指标名称\", \"指标要求\", \"验证方式\"]}', 1, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (6, 211, 1, '{\"text\": \"本项目采用微服务与云原生架构，整体逻辑架构分为感知层、数据层、平台层和应用层，如下图所示：\"}', 1, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (7, 211, 3, '{\"url\": \"https://img.example.com/smart-city-arch.png\", \"name\": \"智慧城市总体逻辑架构图\", \"width\": 1920, \"height\": 1080}', 2, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (8, 212, 1, '{\"text\": \"事件统一从感知层上报至平台层，经规则引擎自动分发至对应委办局子系统，办理完成后数据回流闭环。\"}', 1, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (9, 221, 2, '{\"name\": \"服务器配置清单\", \"rows\": [[\"核心数据库服务器\", \"4台\", \"256G内存 10T NVMe SSD\"], [\"应用服务器\", \"20台\", \"64核 128G内存\"]], \"headers\": [\"设备名称\", \"数量\", \"规格参数\"]}', 1, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (10, 222, 2, '{\"name\": \"网络设备配置清单\", \"rows\": [[\"万兆核心交换机\", \"2台\", \"背板带宽2.56Tbps\"], [\"千兆接入交换机\", \"10台\", \"48口全千兆 PoE+\"]], \"headers\": [\"设备名称\", \"数量\", \"规格参数\"]}', 1, '2026-03-28 09:16:12', '2026-03-28 09:16:12');
INSERT INTO `tdw_contents` VALUES (11, 131, 1, '{\"text\": \"本项目的总体目标是打造全市统一的城市大脑中枢，实现“一屏观全城、一网管全城、一端惠全城”的建设目标。\"}', 1, '2026-03-28 10:07:00', '2026-03-28 10:07:00');
INSERT INTO `tdw_contents` VALUES (12, 132, 1, '{\"text\": \"项目分三个阶段推进，各阶段的建设目标如下：\"}', 1, '2026-03-28 10:07:00', '2026-03-28 10:07:00');
INSERT INTO `tdw_contents` VALUES (13, 132, 2, '{\"name\": \"建设阶段目标表\", \"rows\": [[\"一期\", \"2026.06-2026.12\", \"核心平台搭建\", \"数据中台上线，支撑3个委办局接入\"], [\"二期\", \"2027.01-2027.06\", \"全领域覆盖\", \"8大领域全部接入，实现一网统管\"], [\"三期\", \"2027.07-2027.12\", \"智能升级\", \"AI大模型赋能，实现事件自动处置率80%以上\"]], \"headers\": [\"阶段\", \"时间\", \"建设内容\", \"验收标准\"]}', 2, '2026-03-28 10:07:00', '2026-03-28 10:07:00');

-- ----------------------------
-- Table structure for tdw_outline_closure
-- ----------------------------
DROP TABLE IF EXISTS `tdw_outline_closure`;
CREATE TABLE `tdw_outline_closure`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ancestor` bigint(0) NOT NULL COMMENT '祖先节点ID',
  `descendant` bigint(0) NOT NULL COMMENT '后代节点ID',
  `depth` int(0) NOT NULL COMMENT '祖先到后代的深度，0代表节点自己',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ancestor_descendant`(`ancestor`, `descendant`) USING BTREE,
  INDEX `idx_ancestor`(`ancestor`) USING BTREE,
  INDEX `idx_descendant`(`descendant`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '大纲树形闭包关系表，存储所有祖先后代关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tdw_outline_closure
-- ----------------------------
INSERT INTO `tdw_outline_closure` VALUES (1, 10, 10, 0);
INSERT INTO `tdw_outline_closure` VALUES (2, 10, 11, 1);
INSERT INTO `tdw_outline_closure` VALUES (3, 11, 11, 0);
INSERT INTO `tdw_outline_closure` VALUES (4, 10, 111, 2);
INSERT INTO `tdw_outline_closure` VALUES (5, 11, 111, 1);
INSERT INTO `tdw_outline_closure` VALUES (6, 111, 111, 0);
INSERT INTO `tdw_outline_closure` VALUES (7, 10, 112, 2);
INSERT INTO `tdw_outline_closure` VALUES (8, 11, 112, 1);
INSERT INTO `tdw_outline_closure` VALUES (9, 112, 112, 0);
INSERT INTO `tdw_outline_closure` VALUES (10, 10, 12, 1);
INSERT INTO `tdw_outline_closure` VALUES (11, 12, 12, 0);
INSERT INTO `tdw_outline_closure` VALUES (12, 10, 121, 2);
INSERT INTO `tdw_outline_closure` VALUES (13, 12, 121, 1);
INSERT INTO `tdw_outline_closure` VALUES (14, 121, 121, 0);
INSERT INTO `tdw_outline_closure` VALUES (15, 10, 122, 2);
INSERT INTO `tdw_outline_closure` VALUES (16, 12, 122, 1);
INSERT INTO `tdw_outline_closure` VALUES (17, 122, 122, 0);
INSERT INTO `tdw_outline_closure` VALUES (18, 20, 20, 0);
INSERT INTO `tdw_outline_closure` VALUES (19, 20, 21, 1);
INSERT INTO `tdw_outline_closure` VALUES (20, 21, 21, 0);
INSERT INTO `tdw_outline_closure` VALUES (21, 20, 211, 2);
INSERT INTO `tdw_outline_closure` VALUES (22, 21, 211, 1);
INSERT INTO `tdw_outline_closure` VALUES (23, 211, 211, 0);
INSERT INTO `tdw_outline_closure` VALUES (24, 20, 212, 2);
INSERT INTO `tdw_outline_closure` VALUES (25, 21, 212, 1);
INSERT INTO `tdw_outline_closure` VALUES (26, 212, 212, 0);
INSERT INTO `tdw_outline_closure` VALUES (27, 20, 22, 1);
INSERT INTO `tdw_outline_closure` VALUES (28, 22, 22, 0);
INSERT INTO `tdw_outline_closure` VALUES (29, 20, 221, 2);
INSERT INTO `tdw_outline_closure` VALUES (30, 22, 221, 1);
INSERT INTO `tdw_outline_closure` VALUES (31, 221, 221, 0);
INSERT INTO `tdw_outline_closure` VALUES (32, 20, 222, 2);
INSERT INTO `tdw_outline_closure` VALUES (33, 22, 222, 1);
INSERT INTO `tdw_outline_closure` VALUES (34, 222, 222, 0);
INSERT INTO `tdw_outline_closure` VALUES (35, 10, 13, 1);
INSERT INTO `tdw_outline_closure` VALUES (36, 13, 13, 0);
INSERT INTO `tdw_outline_closure` VALUES (37, 10, 131, 2);
INSERT INTO `tdw_outline_closure` VALUES (38, 13, 131, 1);
INSERT INTO `tdw_outline_closure` VALUES (39, 131, 131, 0);
INSERT INTO `tdw_outline_closure` VALUES (40, 10, 132, 2);
INSERT INTO `tdw_outline_closure` VALUES (41, 13, 132, 1);
INSERT INTO `tdw_outline_closure` VALUES (42, 132, 132, 0);

-- ----------------------------
-- Table structure for tdw_outlines
-- ----------------------------
DROP TABLE IF EXISTS `tdw_outlines`;
CREATE TABLE `tdw_outlines`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bid_id` bigint(0) NOT NULL COMMENT '所属标书ID',
  `parent_id` bigint(0) DEFAULT NULL COMMENT '父节点ID',
  `level` int(0) NOT NULL COMMENT '层级（1:章, 2:节, 3:内容标题）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '大纲标题',
  `sort_num` int(0) NOT NULL COMMENT '同级排序号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_bid_id`(`bid_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '大纲节点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tdw_outlines
-- ----------------------------
INSERT INTO `tdw_outlines` VALUES (10, 1001, NULL, 1, '第一章 项目背景与需求分析', 1);
INSERT INTO `tdw_outlines` VALUES (11, 1001, 10, 2, '第一节 建设背景', 1);
INSERT INTO `tdw_outlines` VALUES (12, 1001, 10, 2, '第二节 需求分析', 2);
INSERT INTO `tdw_outlines` VALUES (13, 1001, 10, 2, '第三节 项目目标', 3);
INSERT INTO `tdw_outlines` VALUES (20, 1001, NULL, 1, '第二章 总体建设方案', 2);
INSERT INTO `tdw_outlines` VALUES (21, 1001, 20, 2, '第一节 系统架构设计', 1);
INSERT INTO `tdw_outlines` VALUES (22, 1001, 20, 2, '第二节 硬件配置清单', 2);
INSERT INTO `tdw_outlines` VALUES (111, 1001, 11, 3, '(内容一) 政策背景', 1);
INSERT INTO `tdw_outlines` VALUES (112, 1001, 11, 3, '(内容二) 行业现状', 2);
INSERT INTO `tdw_outlines` VALUES (121, 1001, 12, 3, '(内容一) 业务需求', 1);
INSERT INTO `tdw_outlines` VALUES (122, 1001, 12, 3, '(内容二) 性能需求', 2);
INSERT INTO `tdw_outlines` VALUES (131, 1001, 13, 3, '(内容一) 总体目标', 1);
INSERT INTO `tdw_outlines` VALUES (132, 1001, 13, 3, '(内容二) 阶段建设目标', 2);
INSERT INTO `tdw_outlines` VALUES (211, 1001, 21, 3, '(内容一) 逻辑架构图', 1);
INSERT INTO `tdw_outlines` VALUES (212, 1001, 21, 3, '(内容二) 核心业务流程', 2);
INSERT INTO `tdw_outlines` VALUES (221, 1001, 22, 3, '(内容一) 服务器配置', 1);
INSERT INTO `tdw_outlines` VALUES (222, 1001, 22, 3, '(内容二) 网络设备配置', 2);

SET FOREIGN_KEY_CHECKS = 1;
