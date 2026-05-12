package com.ruoyi.tdw.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

public class LlmPromptProperties {

    @Value("${llm.prompt.outline-service}")
    private String outlineService;    // 服务类

    @Value("${llm.prompt.outline-goods}")
    private String outlineGoods;      // 货物类

    @Value("${llm.prompt.outline-engineering}")
    private String outlineEngineering;// 工程类

    @Value("${llm.prompt.outline-supervision}")
    private String outlineSupervision;// 监理类

    @Value("${llm.prompt.outline-it}")
    private String outlineIt;         // IT信息类

    @Value("${llm.prompt.outline-other}")
    private String outlineOther;      // 其它类

    // getter和setter
    public String getOutlineService() { return outlineService; }
    public void setOutlineService(String outlineService) { this.outlineService = outlineService; }
    public String getOutlineGoods() { return outlineGoods; }
    public void setOutlineGoods(String outlineGoods) { this.outlineGoods = outlineGoods; }
    public String getOutlineEngineering() { return outlineEngineering; }
    public void setOutlineEngineering(String outlineEngineering) { this.outlineEngineering = outlineEngineering; }
    public String getOutlineSupervision() { return outlineSupervision; }
    public void setOutlineSupervision(String outlineSupervision) { this.outlineSupervision = outlineSupervision; }
    public String getOutlineIt() { return outlineIt; }
    public void setOutlineIt(String outlineIt) { this.outlineIt = outlineIt; }
    public String getOutlineOther() { return outlineOther; }
    public void setOutlineOther(String outlineOther) { this.outlineOther = outlineOther; }
}
