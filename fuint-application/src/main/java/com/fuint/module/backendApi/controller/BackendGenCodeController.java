package com.fuint.module.backendApi.controller;

import com.fuint.common.dto.AccountInfo;
import com.fuint.common.service.GenCodeService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessRuntimeException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.common.Constants;
import com.fuint.common.enums.StatusEnum;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.TGenCode;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成管理类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-代码生成相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/genCode")
public class BackendGenCodeController extends BaseController {

    /**
     * 生成代码服务接口
     */
    private GenCodeService genCodeService;

    /**
     * 代码生成列表
     *
     * @param  request HttpServletRequest对象
     * @return 代码生成列表
     */
    @ApiOperation(value = "代码生成列表查询")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:genCode:list')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String title = request.getParameter("title");
        String status = request.getParameter("status");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotEmpty(title)) {
            params.put("title", title);
        }
        if (StringUtil.isNotEmpty(status)) {
            params.put("status", status);
        }
        paginationRequest.setSearchParams(params);
        PaginationResponse<TGenCode> paginationResponse = genCodeService.queryGenCodeListByPagination(paginationRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("dataList", paginationResponse);

        return getSuccessResult(result);
    }

    /**
     * 更新代码生成状态
     *
     * @return
     */
    @ApiOperation(value = "更新代码状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:genCode:edit')")
    public ResponseObject updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String status = params.get("status") != null ? params.get("status").toString() : StatusEnum.ENABLED.getKey();
        Integer id = params.get("id") == null ? 0 : Integer.parseInt(params.get("id").toString());

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        TGenCode tGenCode = genCodeService.queryGenCodeById(id);
        if (tGenCode == null) {
            return getFailureResult(201);
        }

        tGenCode.setOperator(accountInfo.getAccountName());
        tGenCode.setId(id);
        tGenCode.setStatus(status);
        genCodeService.updateGenCode(tGenCode);

        return getSuccessResult(true);
    }

    /**
     * 保存代码生成
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "保存代码生成")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:genCode:add')")
    public ResponseObject saveHandler(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String id = params.get("id") == null ? "" : params.get("id").toString();
        String status = params.get("status") == null ? "" : params.get("status").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        TGenCode tGenCode = new TGenCode();
        tGenCode.setOperator(accountInfo.getAccountName());
        tGenCode.setStatus(status);
        if (StringUtil.isNotEmpty(id)) {
            tGenCode.setId(Integer.parseInt(id));
            genCodeService.updateGenCode(tGenCode);
        } else {
            genCodeService.addGenCode(tGenCode);
        }

        return getSuccessResult(true);
    }

    /**
     * 获取代码生成详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "获取代码生成详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:genCode:list')")
    public ResponseObject info(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        TGenCode tGenCode = genCodeService.queryGenCodeById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("tGenCode", tGenCode);

        return getSuccessResult(result);
    }

    /**
     * 生成代码
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "生成代码")
    @RequestMapping(value = "/gen", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject gen(HttpServletRequest request) throws BusinessRuntimeException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            // empty
        }

        genCodeService.generatorCode("mt_luck");
        return getSuccessResult(true);
    }
}
