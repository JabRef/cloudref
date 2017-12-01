import { ActivatedRoute, Router } from '@angular/router';
import { DefaultApi } from '../../../../../../gen/api/DefaultApi';
import { Comment } from '../../../../../../gen/model/Comment';
import { NgForm } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import { trigger, style, transition, animate } from '@angular/animations';
import { getErrorMessage } from '../../../../../../shared/errorHandler';

@Component({
  selector: 'comments',
  styleUrls: ['./pdf.scss'],
  templateUrl: './pdf.html',
  animations: [
    trigger('anim', [
      transition(':enter', [
        style({transform: 'translateX(-10%)', opacity: 0}),
        animate('0.2s', style({transform: 'translateX(0%)', opacity: 1})),
      ]),
      transition(':leave', [
        animate('0.2s', style({transform: 'translateX(-10%)', opacity: 0})),
      ]),
    ]),
    trigger('fadeInOut', [
      transition(':enter', [
        style({opacity: 0}),
        animate('0.2s', style({opacity: 1})),
      ]),
      transition(':leave', [
        animate('0.2s', style({opacity: 0})),
      ]),
    ]),
  ],
})

export class PdfViewComponent implements AfterViewInit, OnDestroy {

  id: string;
  private subParam: any;
  comments: Comment[];
  newComment: Comment;
  pdfUrl: string = '';
  hasPdf: boolean = false;
  back: Function;
  addComment: Function;
  viewerUrl: SafeResourceUrl;
  private currentUser: string;
  httpErrorMsg = null;

  deleteId: number;

  pdfViewerScroll: HTMLDivElement;
  inputElementPage: HTMLInputElement;
  inputElementPageNumber: HTMLSpanElement;

  listenerIFrame;
  listenerWindow;

  private selectedComment: number;
  private tempSelectedCommentContent: string;
  private tempSelectedCommentPublish: boolean;
  private selectedCommentIndex: number
  private hiddenModal: boolean = true;

  searchableList = ['author', 'content'];
  sort: string = 'pageNumber';
  private queryString: string = null;
  private lastPageNumberLabel: number;
  private searchInputValue: string;
  private showAll: boolean = false;
  private disableShowAll: boolean = false;

  private triggerShowAll() {
    // toggle
    this.showAll = !this.showAll;

    if (this.showAll) {
      // show all comments
      this.queryString = '';
    } else {
      // reset show all
      this.queryString = null;
    }
  }

  private searchComments() {
    // check if search input changed
    if (this.queryString !== this.searchInputValue) {

      if (this.searchInputValue != null && this.searchInputValue !== '') {
        // change search string
        this.queryString = this.searchInputValue;
        this.showAll = true;
        this.disableShowAll = true;
      } else {
        // reset search
        this.queryString = null;
        this.showAll = false;
        this.disableShowAll = false;
      }

      // check if a comment is selected
      if (this.selectedComment != null) {
        // deselect comment
        if (this.commentChanged()) {
          this.showModalChangedComment();
        } else {
          // not changed -> reset values
          this.tempSelectedCommentContent = null;
          this.tempSelectedCommentPublish = null;
          this.selectedComment = null;
        }
      }
    }
  }

  /**
   * Trigger search pipe again with the same search string.
   */
  private triggerSearchAgain() {
    // assign new object to trigger pipe again
    this.searchableList = [].concat(this.searchableList);
  }

  private sameAsLastPageNumberLabel(index: number, page: number): boolean {

    if (index !== 0 && this.lastPageNumberLabel === page) {
      return true;
    }
    this.lastPageNumberLabel = page;
    return false;
  }

  private showModalChangedComment() {
    if (this.hiddenModal) {
      this.hiddenModal = false;
      document.getElementById('openModalCommentChangedButton').click();
    }
  }

  private editComment(id: number) {

    if (this.selectedComment == null || (this.selectedComment !== id && !this.commentChanged())) {
      // save comment for reset
      this.selectedCommentIndex = this.comments.findIndex(x => x.id === id);
      this.tempSelectedCommentContent = this.comments[this.selectedCommentIndex].content;
      this.tempSelectedCommentPublish = this.comments[this.selectedCommentIndex].publish;
      this.selectedComment = id;
    } else if (this.selectedComment !== id) {
      this.showModalChangedComment();
    }
  }

  private cancelEditing() {
    // reset comment
    let id = this.comments.findIndex(x => x.id === this.selectedComment);
    this.comments[id].content = this.tempSelectedCommentContent;
    this.comments[id].publish = this.tempSelectedCommentPublish;
    // reset values
    this.tempSelectedCommentContent = null;
    this.tempSelectedCommentPublish = null;
    this.selectedComment = null;
    this.hiddenModal = true;
  }

  private commentChanged(): boolean {
    if (this.selectedComment != null) {
      // find selected comment
      let id = this.comments.findIndex(x => x.id === this.selectedComment);
      // check if comment exists
      if (id >= 0) {
        // check if comment changed
        if (this.tempSelectedCommentPublish !== this.comments[id].publish ||
          this.tempSelectedCommentContent !== this.comments[id].content) {
          return true;
        }
      } else {
        // comment does not exist, delete selection
        this.tempSelectedCommentContent = null;
        this.tempSelectedCommentPublish = null;
        this.selectedComment = null;
      }
    }
    return false;
  }

  private loadPdfAndComments() {
    // get pdf and comments from backend
    this.api.getPdfFile(this.id).subscribe(response => {
        if (response != null) {
          this.hasPdf = true;
          this.pdfUrl = URL.createObjectURL(response);

          let tempViewerUrl = 'assets/pdfjs/web/viewer.html?file=' + encodeURIComponent(this.pdfUrl);
          this.viewerUrl = this.domSanitizer.bypassSecurityTrustResourceUrl(tempViewerUrl);

          // get comments from backend
          this.loadComments(false);
        }
      },
      (err) => {
        this.errorHandler(err);
      },
    );
  }

  private loadComments(resetSelected: boolean) {
    this.api.getPdfComments(this.id).subscribe(result => {
        if (result != null) {
          if (this.comments == null) {
            this.comments = result;
          } else {
            // update array with comments from backend to prevent animation of all comments
            for (let i = 0; i < this.comments.length; i++) {
              // find comment in result
              let id = result.findIndex(x => x.id === this.comments[i].id);
              // update comment
              if (id >= 0) {
                // update all values of comment separately to prevent animation of comment
                this.comments[i].content = result[id].content;
                this.comments[i].publish = result[id].publish;
                this.comments[i].pageNumber = result[id].pageNumber;
                this.comments[i].author = result[id].author;
                this.comments[i].page = result[id].page;
                this.comments[i].alterationDate = result[id].alterationDate;
                this.comments[i].date = result[id].date;
                // delete comment from result array
                result.splice(id, 1);
              } else {
                // comment does not exist at backend -> delete it from the array
                this.comments.splice(i, 1);
                i--;
              }
            }

            // add remaining comments from backend to the array
            Array.prototype.push.apply(this.comments, result);

            if (resetSelected != null && resetSelected) {
              // reset selected comment
              this.selectedComment = null;
            }

            // trigger search pipe again because comments may changed
            if ((this.queryString != null && this.queryString !== '')) {
              this.triggerSearchAgain();
            }
          }
        }
      },
      (err) => {
        this.errorHandler(err);
      },
    );
  }

  constructor(private router: Router, private api: DefaultApi, private route: ActivatedRoute, private domSanitizer: DomSanitizer) {

    let object = localStorage.getItem('CloudRefUser');
    if (object != null) {
      let userInfo = JSON.parse(object);
      this.currentUser = userInfo.username;
    }

    this.listenerIFrame = (event) => {
      // send message to window to inform it about scrolling
      window.postMessage('scrolling', '*');
    }

    this.listenerWindow = (event) => {
      // get current page numbers
      this.loadCurrentPdfPage();
    }

    // get bibtexkey from url
    this.subParam = this.route.params.subscribe(params => {
      this.id = params['id'];
    });

    // initialize form
    this.newComment = {
      id: 100,
      bibtexkey: this.id,
      author: this.currentUser,
      publish: true,
      content: '',
      page: '',
      pageNumber: -1,
    };

    // get pdf and comments from backend
    this.loadPdfAndComments();

    this.back = function () {
      this.router.navigate(['references', this.id]);
    };

    this.addComment = function (f: NgForm) {
      // check if form content is valid
      if (f.valid) {
        this.api.saveComment(this.newComment, this.id).subscribe(
          data => {
            // close modal
            document.getElementById('closeModal').click();
            // reset fields of form
            this.resetForm(f);
            this.loadComments(false);
          },
          (err) => {
            this.errorHandler(err);

            // check if status does not contain no response from backend
            if (err.status !== 0) {
              // reload comments
              this.loadComments(false);
            }
          },
        );
      }
    };
  }

  /**
   * Triggered if popover is opened and closed.
   * @param idForDelete the comment id on which popover was triggered
   */
  private deleteCommentId(idForDelete: number) {
    if (this.deleteId == null) {
      // set new value -> opened popover
      this.deleteId = idForDelete;
    } else if (this.deleteId === idForDelete) {
      // same value -> closed popover
      this.deleteId = null;
    } else {
      // id changed -> close old popover
      this.closePopover();
      // set new id
      this.deleteId = idForDelete;
    }
  }

  /**
   * Close popover manually if user opens another one.
   */
  private closePopover() {
    let buttonDelete = <HTMLButtonElement> document.getElementById(this.deleteId.toString());
    buttonDelete.click();
    this.deleteId = null;
  }

  /**
   * Delete comment at backend.
   */
  private deleteCommentBackend() {

    this.api.deleteComment(this.id, this.deleteId).subscribe(
      (data) => {
        // successfully deleted comment at backend
        // load comments
        this.loadComments(true);

        // close popover
        this.closePopover();
      },
      (err) => {
        this.errorHandler(err);

        // check if status does not contain no response from backend
        if (err.status !== 0) {
          // reload comments
          this.loadComments(false);
        }
      },
    );
  }

  /**
   * Update a comment.
   *
   * @param id the id of the comment shich should be updated
   */
  private updateComment(id: number) {
    // save changes at backend
    let index = this.comments.findIndex(x => x.id === id);
    let updatedComment: Comment = this.comments[index];
    this.api.updateComment(updatedComment, this.id, updatedComment.id).subscribe((data) => {
        // // update comment in array, prevent animation of comment
        let result: Comment = <Comment> data;
        this.comments[index].content = result.content;
        this.comments[index].publish = result.publish;
        this.comments[index].pageNumber = result.pageNumber;
        this.comments[index].author = result.author;
        this.comments[index].page = result.page;
        this.comments[index].alterationDate = result.alterationDate;
        this.comments[index].date = result.date;
        this.selectedComment = null;
        this.hiddenModal = true;
      },
      (err) => {
        this.errorHandler(err);

        // check if status does not contain no response from backend
        if (err.status !== 0) {
          // reload comments
          this.loadComments(false);
        }
      },
    );
  }

  resetForm(f: NgForm) {
    f.resetForm({isPublic: true});
  }

  private errorHandler(err: any) {
    // show error to user
    this.httpErrorMsg = getErrorMessage(err);
  }

  /**
   * Called if iFrame is loaded. Check if PDF viewer element exists, if yes add listener to scroll event.
   **/
  private loadedIFrame(): void {
    try {
      // check if iFrame element is loaded
      this.pdfViewerScroll = <HTMLDivElement> (<HTMLIFrameElement> document.getElementById('viewerIFrame'))
        .contentWindow.document.getElementById('viewerContainer');
      if (this.pdfViewerScroll != null) {
        // add listener to scroll event
        this.pdfViewerScroll.addEventListener('scroll', this.listenerIFrame);

        // get current page numbers
        this.loadCurrentPdfPage();
      }
    } catch (e) {
      // not loaded yet
    }
  }

  currentPageNumber: number = -1;

  /**
   * Get current shown real page number and sequential page number of PDF.
   **/
  private loadCurrentPdfPage(): void {
    try {
      this.inputElementPage = <HTMLInputElement>(<HTMLIFrameElement> document.getElementById('viewerIFrame'))
        .contentWindow.document.getElementById('pageNumber');
      this.inputElementPageNumber = <HTMLSpanElement>(<HTMLIFrameElement> document.getElementById('viewerIFrame'))
        .contentWindow.document.getElementById('numPages');

      if (this.inputElementPage != null && this.inputElementPageNumber != null) {
        // get current page from PDF viewer
        this.newComment.page = this.inputElementPage.value;

        // get continuous page number
        let tempNumberString = this.inputElementPageNumber.innerText;
        let regexp: RegExp = /[0-9]*\svon\s[0-9]*/;
        if (regexp.test(tempNumberString)) {
          this.newComment.pageNumber = Number(tempNumberString.substring(1, tempNumberString.indexOf(' ')));
        } else {
          this.newComment.pageNumber = Number(this.newComment.page);
        }

        let tempValue = this.newComment.pageNumber;

        // set current page if changed
        if (this.currentPageNumber === -1 || this.currentPageNumber !== this.newComment.pageNumber) {
          // remove page number -> fade out comments of page
          this.currentPageNumber = -1;

          // show modal if comment was edited and search mode is not activated
          // if search mode is activated the comment is not hidden on change of PDF page
          if (!(this.queryString != null && this.queryString !== '') && this.commentChanged()) {
            this.showModalChangedComment();
          }

          // set new page number after timeout -> fade in comments after comments from other page are invisible
          setTimeout(() => {
            // deselect comment if page changes, search is deactivated and comment is not modified
            if (!(this.queryString != null && this.queryString !== '') && !this.commentChanged()) {
              this.selectedComment = null;
            }

            // filter out fast scrolling
            if (tempValue === this.newComment.pageNumber) {
              // set new page number
              this.currentPageNumber = this.newComment.pageNumber;
            }
          }, 300); // 0.3sec
        }
      }
    } catch (e) {
      // elements not loaded
    }
  }

  ngAfterViewInit() {
    // add listener to window to get informed about page changes of iframe content
    window.addEventListener('message', this.listenerWindow);
  }

  ngOnDestroy() {
    // close modals if page is changed
    document.getElementById('closeModalChanges').click();
    document.getElementById('closeModal').click();

    // remove event listener
    try {
      this.pdfViewerScroll.removeEventListener('scroll', this.listenerIFrame);
      window.removeEventListener('message', this.listenerWindow);
    } catch (e) {
      // listener not defined because no PDF exists for reference
    }

    // release object URL of PDF file
    try {
      if (this.pdfUrl !== '') {
        URL.revokeObjectURL(this.pdfUrl);
      }
    } catch (e) {
      console.log(e);
    }
    // unsubscribe from url parameter id
    this.subParam.unsubscribe();
  }
}
