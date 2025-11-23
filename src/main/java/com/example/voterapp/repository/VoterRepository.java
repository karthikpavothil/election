package com.example.voterapp.repository;

import com.example.voterapp.entity.Voter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoterRepository extends JpaRepository<Voter, Integer> {

    List<Voter> findByVoted(boolean voted);

    List<Voter> findByConfirmed(boolean confirmed);

    List<Voter> findByPartyIgnoreCase(String party);
    List<Voter> findByCommentsIgnoreCase(String comments);
    List<Voter> findByRemarksIgnoreCase(String remarks);

    @Query("select distinct v.party from Voter v where v.party is not null and v.party <> '' order by v.party")
    List<String> findDistinctParties();

    @Query("select distinct v.comments from Voter v where v.comments is not null and v.comments <> '' order by v.comments")
    List<String> findDistinctComments();

    @Query("select distinct v.remarks from Voter v where v.remarks is not null and v.remarks <> '' order by v.remarks")
    List<String> findDistinctRemarks();

    @Query("select v.party as party, count(v) as count from Voter v " +
           "where v.voted = true and v.party is not null and v.party <> '' " +
           "group by v.party order by v.party")
    List<PartyVoteCount> countVotedByParty();

    interface PartyVoteCount {
        String getParty();
        long getCount();
    }

}
