import { Component, OnDestroy } from '@angular/core';
import { DefaultApi } from '../../../../../../gen/api/DefaultApi';
import { ActivatedRoute, Router } from '@angular/router';
import { Rating } from '../../../../../../gen/model/Rating';
import UserRatingEnum = Rating.UserRatingEnum;
import { User } from '../../../../../../gen/model/User';
import UserRoleEnum = User.UserRoleEnum;
import { MergeInstruction } from '../../../../../../gen/model/MergeInstruction';
import MergeInstructionEnum = MergeInstruction.MergeInstructionEnum;
import { trigger, transition, animate, style, query, group } from '@angular/animations';
import { ResponseRatingSuggestion } from '../../../../../../gen/model/ResponseRatingSuggestion';
import MergedEnum = ResponseRatingSuggestion.MergedEnum;
import { getErrorMessage } from '../../../../../../shared/errorHandler';

@Component({
  selector: 'suggestions',
  styleUrls: ['./suggestions.scss'],
  templateUrl: './suggestions.html',
  animations: [
    trigger('anim', [
      // remove elements
      transition('* => void', [
        style({height: '*', opacity: '1', overflow: 'hidden'}),
        // decrease opacity of all elements
        animate('0.2s', style({height: '*', overflow: 'hidden', opacity: '.5'})),
        group([
          // decrease size of all elements except rating buttons to height of rating div
          query(':self, .card', [
            animate('0.3s', style({height: '225px', overflow: 'hidden', opacity: '.4'})),
          ]),
          // hide rating buttons
          query('#rating', [
            animate('0.3s', style({opacity: 0})),
          ]),
        ]),
        // hide all remaining elements and decrease their size to zero
        query(':self, .card', [
          animate('0.2s', style({height: '0px', overflow: 'hidden', opacity: 0})),
        ]),
      ]),
      // show elements
      transition('void => *', [
        style({opacity: '0'}),
        // increase opacity of all elements
        animate('0.2s', style({height: '*', opacity: 1})),
      ]),
    ]),
  ],
})

export class SuggestionsComponent implements OnDestroy {

  id: string;
  private subParam: any;
  httpErrorMsg = null;

  private loading: boolean = true;
  private entries;
  // reference on master branch
  private master: Map<any, any> = new Map<any, any>();
  private ratingUser: Map<string, boolean> = new Map<string, boolean>();
  private overallRating: Map<string, number> = new Map<string, number>();
  // fieldnames of reference and suggestion for id of suggestion
  private labelsArray: Map<string, string[]> = new Map<string, string[]>();
  private userRole: String;

  constructor(private router: Router, private api: DefaultApi, private route: ActivatedRoute) {

    let object = localStorage.getItem('CloudRefUser');
    if (object != null) {
      let userInfo = JSON.parse(object);
      this.userRole = userInfo.role;
    }

    // get bibtexkey from url
    this.subParam = this.route.params.subscribe(params => {
      this.id = params['id'];
    });

    this.getSuggestionsFromBackend();
  }

  getSuggestionsFromBackend() {
    // get suggestions from backend
    this.api.getSuggestions(this.id).subscribe(res => {
        let outer = new Map<string, Map<string, string>>();
        let labels = new Map<string, Set<string>>();
        let labelsMaster = new Set<string>();

        for (let r of res) {
          let sug = this.objToStrMap(r);
          // get id
          let id = sug.get('ID');
          if (id != null) {
            sug.delete('ID');

            // get overall rating
            let overallR = sug.get('OverallRating');
            if (overallR != null) {
              sug.delete('OverallRating');
              this.overallRating.set(id, Number(overallR));
            }

            // get rating of user
            let rating = sug.get('RatedByUser');
            if (rating != null) {
              sug.delete('RatedByUser');
              let rat = false;
              if (rating === 'POSITIVE') {
                rat = true;
              }
              this.ratingUser.set(id, rat);
            }
            // save field names
            let labelsSet = new Set<any>();
            sug.forEach((value: any, key: string) => {
              labelsSet.add(key);
            });
            labels.set(id, labelsSet);

            // save to map
            outer.set(id, sug);
          } else {
            // save field names
            sug.forEach((value: any, key: string) => {
              labelsMaster.add(key);
            });
            // save reference from master branch
            this.master = sug;
          }
        }

        if (this.entries != null) {
          // update array with suggestions
          var newSuggestions = Array.from(outer.entries());
          // update values
          let updated: boolean;
          for (let i = 0; i < this.entries.length; i++) {
            updated = false;
            for (let j = 0; j < newSuggestions.length; j++) {
              // check if IDs are the same
              if ((this.entries[i])[0] === (newSuggestions[j])[0]) {
                // update content of suggestion
                updated = true;
                (this.entries[i])[1] = (newSuggestions[j])[1];
                // delete suggestion from array
                newSuggestions.splice(j, 1);
                break;
              }
            }

            if (!updated) {
              // delete suggestion
              this.entries.splice(i, 1);
              i--;
            }
          }
          // add remaining suggestions from backend
          Array.prototype.push.apply(this.entries, newSuggestions);

        } else {
          this.entries = Array.from(outer.entries());
        }

        // add labels of master to all suggestions
        if (labelsMaster.size > 0 && labels.size > 0) {
          labels.forEach((value: any, key: string) => {
            labelsMaster.forEach(item => {
              labels.get(key).add(item);
            });
          });
        }

        labels.forEach((value: Set<string>, key: string) => {
          this.labelsArray.set(key, this.getOrderedEntries(value));
        });

        // finished loading suggestions
        this.loading = false;

      },
      (err) => {
        this.errorHandler(err);
      },
    );

  }

  getOrderedEntries(set: Set<string>): string[] {
    return Array.from(set).sort((s1, s2) => {
      // print type and key first
      if (s1 == 'Type') {
        return -1;
      } else if (s2 == 'Type') {
        return 1;
      }
      if (s1 == 'BibTeX key') {
        return -1;
      } else if (s2 == 'BibTeX key') {
        return 1;
      }
      // compare values
      if (s1 > s2) {
        return 1;
      }
      if (s1 < s2) {
        return -1;
      }
      return 0;
    });
  }

  objToStrMap(obj) {
    let strMap = new Map();
    for (let k of Object.keys(obj)) {
      strMap.set(k, obj[k]);
    }
    return strMap;
  }

  rateSuggestion(positive: boolean, id: string) {
    // check if rating changed
    if (positive != null && this.ratingUser.get(id) != positive) {
      let rat: Rating;
      if (positive) {
        rat = {
          userRating: UserRatingEnum.POSITIVE,
        };
      } else {
        rat = {
          userRating: UserRatingEnum.NEGATIVE,
        };
      }
      this.api.rateSuggestion(this.id, Number(id), rat).subscribe(res => {
          if (res.merged != null) {
            // suggestion was merged
            if (res.merged === MergedEnum.ACCEPT) {
              // accepted suggestion
              // get all suggestions from backend because reference changed
              this.getSuggestionsFromBackend();
            } else {
              // rejected suggestion
              // delete from entries
              this.removeSuggestion(id);
            }
          } else {
            // suggestion stays in system, it was not merged
            this.ratingUser.set(id, positive);
            this.overallRating.set(id, res.overallRating);
          }
        },
        (err) => {
          this.errorHandler(err);
        },
      );
    }
  }

  /**
   * Delete a suggestion and its ratings in the local array and maps.
   * @param id the identifier of the suggestion.
   */
  removeSuggestion(id: string) {
    // find suggestion in array
    for (let i = 0; i < this.entries.length; i++) {
      if ((this.entries[i])[0] === id) {
        this.entries.splice(i, 1);
        break;
      }
    }
    this.ratingUser.delete(id);
    this.overallRating.delete(id);
    this.labelsArray.delete(id);
  }

  mergeSuggestion(accept: boolean, id: string) {
    let instruction;
    if (accept) {
      instruction = {
        mergeInstruction: MergeInstructionEnum.ACCEPT,
      };
    } else {
      instruction = {
        mergeInstruction: MergeInstructionEnum.REJECT,
      };
    }
    this.api.mergeSuggestion(this.id, Number(id), instruction).subscribe(res => {
        if (accept) {
          // load new suggestions because reference on master changed
          this.getSuggestionsFromBackend();
        } else {
          // remove suggestion from view
          this.removeSuggestion(id);
        }
      },
      (err) => {
        this.errorHandler(err);
      },
    );
  }

  viewReference() {
    this.router.navigate(['references', this.id]);
  }

  private errorHandler(err: any) {
    // show error to user
    this.httpErrorMsg = getErrorMessage(err);
  }

  ngOnDestroy() {
    this.subParam.unsubscribe();
  }
}
