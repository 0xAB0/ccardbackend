package march.controllers;

import march.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/statement")
public class StatementController {

    @Autowired
    private Model model;

    @GetMapping("/list")
    public String [] listStatements() {
        return model.getStatementNames();
    }
}
