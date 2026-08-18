package com.hei.school.endpoint.web;

import com.hei.school.service.PromotionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class PromotionWebController {

  private final PromotionService promotionService;

  /** The promotions page is the only screen, so it is what the root should open. */
  @GetMapping("/")
  public String home() {
    return "redirect:/promotions";
  }

  @GetMapping("/promotions")
  public String listPromotions(Model model) {
    model.addAttribute("promotionYears", promotionService.listPromotionYears());
    return "promotions";
  }
}
