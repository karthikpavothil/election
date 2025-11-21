package com.example.voterapp.controller;

import com.example.voterapp.entity.Voter;
import com.example.voterapp.repository.VoterRepository;
import com.example.voterapp.repository.VoterRepository.PartyVoteCount;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/voters")
public class VoterController {

    private final VoterRepository voterRepository;

    public VoterController(VoterRepository voterRepository) {
        this.voterRepository = voterRepository;
    }

    @GetMapping
    public String listVoters(@RequestParam(name = "voted", required = false) Optional<Boolean> voted,
                             @RequestParam(name = "confirmed", required = false) Optional<Boolean> confirmed,
                             @RequestParam(name = "party", required = false) Optional<String> party,
                             Model model) {

        List<Voter> voters;

        if (party.isPresent() && !party.get().isBlank()) {
            voters = voterRepository.findByPartyIgnoreCase(party.get());
        } else if (voted.isPresent()) {
            voters = voterRepository.findByVoted(voted.get());
        } else if (confirmed.isPresent()) {
            voters = voterRepository.findByConfirmed(confirmed.get());
        } else {
            voters = voterRepository.findAll();
        }

        List<String> parties = voterRepository.findDistinctParties();

        model.addAttribute("voters", voters);
        model.addAttribute("selectedVoted", voted.orElse(null));
        model.addAttribute("selectedConfirmed", confirmed.orElse(null));
        model.addAttribute("selectedParty", party.orElse(""));
        model.addAttribute("parties", parties);

        return "voters";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("voter", new Voter());
        return "voter-form";
    }

    @PostMapping
    public String saveVoter(@ModelAttribute Voter voter) {
        voterRepository.save(voter);
        return "redirect:/voters";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Voter voter = voterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid voter Id:" + id));
        model.addAttribute("voter", voter);
        return "voter-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteVoter(@PathVariable Integer id) {
        voterRepository.deleteById(id);
        return "redirect:/voters";
    }

    @GetMapping("/chart")
    public String chart(Model model) {
        List<PartyVoteCount> counts = voterRepository.countVotedByParty();
        List<String> labels = counts.stream().map(PartyVoteCount::getParty).collect(Collectors.toList());
        List<Long> values = counts.stream().map(PartyVoteCount::getCount).collect(Collectors.toList());

        model.addAttribute("labels", labels);
        model.addAttribute("values", values);
        return "voter-chart";
    }

    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/voters";
    }

    @GetMapping("/readExcelAndSave")
    public String readExcel() {
        try{
            List<Voter> voters = new ArrayList<>();

            ClassPathResource resource = new ClassPathResource("/voter-list.xlsx");
            InputStream inputStream = resource.getInputStream();

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            boolean skipHeader = true;

            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                Voter dto = new Voter();

                dto.setId((int) row.getCell(0).getNumericCellValue());
                dto.setName(getString(row.getCell(1)));
                dto.setAddress(getString(row.getCell(2)));
                dto.setConfirmed("Yes".equalsIgnoreCase(getString(row.getCell(3)))?true:false);
                dto.setParty(getString(row.getCell(4)));
                dto.setPhone(getString(row.getCell(5)));
                dto.setVoted("Yes".equalsIgnoreCase(getString(row.getCell(6)))?true:false);

                voters.add(dto);
            }

            workbook.close();
            voterRepository.saveAll(voters);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "redirect:/voters";

    }

    private static String getString(Cell cell) {
        return cell == null ? "" : cell.toString().trim();
    }
}
