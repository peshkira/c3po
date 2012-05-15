package com.petpet.c3po.job;

import javax.ejb.Singleton;

@Singleton
public class ProfileJobController {

//  private static final Logger LOG = LoggerFactory.getLogger(ProfileJobController.class);
//
//  @EJB
//  private PersistenceLayer pl;
//
//  private Map<String, ProfileJob> jobs;
//
//  private ExecutorService pool;
//
//  public ProfileJobController() {
//    pool = Executors.newCachedThreadPool();
//    jobs = new HashMap<String, ProfileJob>();
//    LOG.warn("controller created");
//  }
//
//  public String submit(String collection, List<String> expanded) {
//    PreparedQueries pq = new PreparedQueries(pl.getEntityManager());
//
//    ProfileJob job = new ProfileJob(pq, collection, expanded);
//    if (!job.getId().equals(ProfileJob.ERROR_ID)) {
//      jobs.put(job.getId(), job);
//      pool.execute(job);
//      LOG.info("adding job to map {}", job.getId());
//    }
//    return job.getId();
//  }
//
//  public JobStatus status(String uuid) {
//    LOG.info("key in controller: {}", uuid);
//    ProfileJob job = this.jobs.get(uuid);
//    JobStatus status = JobStatus.READY;
//
//    if (job == null) {
//      status = JobStatus.NOT_FOUND;
//
//    } else if (job.isRunning()) {
//      status = JobStatus.RUNNING;
//    }
//
//    return status;
//  }
//
//  public enum JobStatus {
//    RUNNING, READY, NOT_FOUND
//  }
//
//  public void remove(String uuid) {
//    this.jobs.remove(uuid);
//  }
}
