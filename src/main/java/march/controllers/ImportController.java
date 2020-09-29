package march.controllers;

import march.Model;
import march.dao.DataResponse;
import march.dao.DataRow;
import march.dao.ImportResponse;
import march.dao.StatusResponse;
import march.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/import")
public class ImportController {

    @Autowired
    private Model model;

    @RequestMapping(value = "/", method= RequestMethod.POST)
    public ImportResponse createImport(@RequestParam(name="name") String name,
                                       @RequestBody String csv) {

        return model.importCsv(name, csv);
    }

    @RequestMapping(value = "/{id}/data", method=RequestMethod.GET)
    public DataResponse getImportData(@PathVariable("id") String id) {

        final Transaction [] txns = model.getPendingTransactions(Integer.parseInt(id));
        final List<DataRow> rows = new ArrayList<>();
        for(int c=0;c<txns.length;++c) {
            final Transaction txn = txns[c];
            rows.add(
                    new DataRow(c, txn.getDate(), txn.getDescription(), txn.getAmount(), txn.getCategory())
            );
        }

        return new DataResponse(rows.toArray(new DataRow[]{}));
    }

    @RequestMapping(value = "/{id}/{rowId}/category", method=RequestMethod.PUT)
    public StatusResponse updateCategory(
            @PathVariable(name="id") String id,
            @PathVariable(name="rowId") int rowId,
            @RequestBody String payload) {

        final Transaction [] txns = model.getPendingTransactions(Integer.parseInt(id));
        txns[rowId].setCategory(payload);

        return new StatusResponse("ok");
    }

    @RequestMapping(value = "/{id}/commit", method=RequestMethod.PUT)
    public StatusResponse commit (@PathVariable(name = "id") String id) {
        model.commitPending(Integer.parseInt(id));
        return new StatusResponse("ok");
    }
}
