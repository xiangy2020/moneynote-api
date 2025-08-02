package cn.biq.mn.balanceflow;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import cn.biq.mn.response.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/balance-flows")
@RequiredArgsConstructor
public class BalanceFlowController {
    private static final Logger log = LoggerFactory.getLogger(BalanceFlowController.class);

    private final BalanceFlowService balanceFlowService;

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody BalanceFlowAddForm form) {
        return new BaseResponse(balanceFlowService.add(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(BalanceFlowQueryForm form, @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable page) {
        // System.out.println("Handling GET request for balance flows");
        // System.out.println("Form data: " + form);
        // System.out.println("Pageable: " + page);
        return new PageResponse<>(balanceFlowService.query(form, page));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public BaseResponse handleGet(@PathVariable("id") Integer id) {
        return new DataResponse<>(balanceFlowService.get(id));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody BalanceFlowUpdateForm form) {
        return new BaseResponse(balanceFlowService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(balanceFlowService.remove(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/statistics")
    public BaseResponse handleStatistics(BalanceFlowQueryForm form) {
        return new DataResponse<>(balanceFlowService.statistics(form));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/confirm")
    public BaseResponse handleConfirm(@PathVariable("id") Integer id) {
        return new BaseResponse(balanceFlowService.confirm(id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/addFile")
    public BaseResponse handleAddFile(
            @PathVariable("id") Integer id,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        
            System.out.println(String.format("Received file upload request, Content-Type: %s", request.getContentType()));

            System.out.println(String.format("File details: name=%s, size=%d, contentType=%s", 
            file.getOriginalFilename(), file.getSize(), file.getContentType()));
            
        return new DataResponse<>(balanceFlowService.addFile(id, file));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}/files")
    public BaseResponse handleFiles(@PathVariable("id") Integer id, HttpServletRequest request) {
          // 打印请求头信息
        System.out.println("Request Headers:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            System.out.println(headerName + ": " + headerValue);
        });
        return new DataResponse<>(balanceFlowService.getFiles(id));
    }

    @PostMapping("/import")
    public BaseResponse handleImport(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            return new BaseResponse(balanceFlowService.importFromExcel(file));
        } catch (IOException e) {
            BaseResponse response = new BaseResponse(false);
            response.setMessage("Failed to process Excel file: " + e.getMessage());
            return response;
        }
    }

}
