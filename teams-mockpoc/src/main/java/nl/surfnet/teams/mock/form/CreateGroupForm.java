package nl.surfnet.teams.mock.form;

public class CreateGroupForm {

  private String product;
  private String quantity;
  private String productvariation;
  private String managermail;
  private boolean sendMailToManager;

  public String getProductvariation() {
    return productvariation;
  }

  public void setProductvariation(String productvariation) {
    this.productvariation = productvariation;
  }


  public String getProduct() {
    return product;
  }
  public void setProduct(String product) {
    this.product = product;
  }

  public String getQuantity() {
    return quantity;
  }
  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }

  public String getManagermail() {
    return managermail;
  }

  public void setManagermail(String managermail) {
    this.managermail = managermail;
  }

  public boolean isSendMailToManager() {
    return sendMailToManager;
  }

  public void setSendMailToManager(boolean sendMailToManager) {
    this.sendMailToManager = sendMailToManager;
  }
}
